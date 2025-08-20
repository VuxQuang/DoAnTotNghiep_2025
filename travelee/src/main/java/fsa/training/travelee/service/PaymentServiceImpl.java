package fsa.training.travelee.service;

import fsa.training.travelee.dto.payment.SepayWebhookDto;
import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.entity.payment.Payment;
import fsa.training.travelee.entity.payment.PaymentStatus;
import fsa.training.travelee.repository.BookingRepository;
import fsa.training.travelee.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final BookingRepository bookingRepository;
	private final EmailService emailService;

	@Override
	@Transactional
	public Booking handleSepayWebhook(SepayWebhookDto payload) {
		log.info("Xử lý webhook SePay: ref={}, status={}, amount={}, transferType={}", 
			payload.getReferenceCode(), payload.getStatus(), payload.getAmount(), payload.getTransferType());
		
		// tham chiếu bằng bookingCode trước, nếu không có thì thử paymentCode
		Payment payment = null;
		if (payload.getReferenceCode() != null) {
			payment = paymentRepository.findByBookingCode(payload.getReferenceCode())
					.orElseGet(() -> paymentRepository.findByPaymentCode(payload.getReferenceCode()).orElse(null));
		}

		if (payment == null) {
			String raw = payload.getDescription();
			if (raw != null) {
				String extracted = extractBookingCode(raw);
				if (extracted != null) {
					payment = paymentRepository.findByBookingCode(extracted).orElse(null);
				}
			}
		}

		if (payment == null) {
			log.error("Không tìm thấy payment theo referenceCode: {}", payload.getReferenceCode());
			throw new IllegalArgumentException("Không tìm thấy payment theo referenceCode");
		}

		Booking booking = payment.getBooking();
		log.info("Tìm thấy booking: {} với trạng thái hiện tại: payment={}, booking={}", 
			booking.getBookingCode(), payment.getStatus(), booking.getStatus());

		// Xác nhận trạng thái từ SePay
		// Ưu tiên status = SUCCESS; nếu thiếu status, chấp nhận transferType = in như thanh toán vào
		boolean success = (payload.getStatus() != null && payload.getStatus().equalsIgnoreCase("SUCCESS"))
				|| (payload.getStatus() == null && payload.getTransferType() != null && payload.getTransferType().equalsIgnoreCase("in"));
		boolean amountMatched = payload.getAmount() != null && booking.getTotalAmount() != null
				&& payload.getAmount().compareTo(booking.getTotalAmount()) == 0;

		log.info("Đánh giá webhook: success={}, amountMatched={}, expectedAmount={}, actualAmount={}", 
			success, amountMatched, booking.getTotalAmount(), payload.getAmount());

		if (success && amountMatched) {
			// Cập nhật payment
			payment.setStatus(PaymentStatus.COMPLETED);
			payment.setTransactionId(payload.getTransactionId());
			payment.setPaidAt(LocalDateTime.now());
			Payment savedPayment = paymentRepository.save(payment);
			log.info("Đã cập nhật payment {} thành COMPLETED", savedPayment.getId());

			// Cập nhật booking thành PAID theo nghiệp vụ
			booking.setStatus(BookingStatus.PAID);
			booking.setPayment(savedPayment); // Đảm bảo reference được cập nhật
			Booking savedBooking = bookingRepository.save(booking);
			log.info("Đã cập nhật booking {} thành PAID", savedBooking.getId());
			try {
				emailService.sendBookingPaidEmail(savedBooking);
			} catch (Exception e) {
				log.error("Lỗi gửi email thanh toán thành công: {}", e.getMessage());
			}
			return savedBooking;
		} else if (!success) {
			payment.setStatus(PaymentStatus.FAILED);
			paymentRepository.save(payment);
			log.warn("Webhook SePay thất bại cho bookingCode={} status={}", booking.getBookingCode(), payload.getStatus());
			return booking;
		} else {
			// success nhưng lệch số tiền
			log.error("Số tiền thanh toán không khớp cho bookingCode={} expected={} actual={}",
					booking.getBookingCode(), booking.getTotalAmount(), payload.getAmount());
			return booking;
		}
	}

	private String extractBookingCode(String text) {
		// BookingCode định dạng BKyyyyMMddHHmmssNNNN theo generateBookingCode()
		// Bắt chuỗi bắt đầu bằng BK và có độ dài tối thiểu 2+14+4 = 20
		if (text == null) return null;
		text = text.toUpperCase();
		int idx = text.indexOf("BK");
		if (idx == -1) return null;
		// lấy 20-24 ký tự kể từ BK để đủ timestamp + random
		int end = Math.min(text.length(), idx + 22);
		String candidate = text.substring(idx, end).replaceAll("[^A-Z0-9]", "");
		if (candidate.length() >= 18 && candidate.startsWith("BK")) {
			return candidate;
		}
		return null;
	}
}


