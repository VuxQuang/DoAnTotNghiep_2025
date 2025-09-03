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
    log.info("Xử lý webhook SePay: ref={}, status={}, amount={}, transferType={}, content={}, description={}", 
        payload.getReferenceCode(), payload.getStatus(), payload.getAmount(), payload.getTransferType(), 
        payload.getContent(), payload.getDescription());

    Payment payment = null;

    // 🔎 Ưu tiên tìm theo referenceCode trước
    if (payload.getReferenceCode() != null && !payload.getReferenceCode().trim().isEmpty()) {
        log.info("Tìm payment theo referenceCode: '{}'", payload.getReferenceCode());
        
        // Thử tìm theo paymentCode trước
        payment = paymentRepository.findByPaymentCode(payload.getReferenceCode()).orElse(null);
        if (payment != null) {
            log.info("Tìm thấy payment theo paymentCode={} -> paymentId={}", payload.getReferenceCode(), payment.getId());
        } else {
            // Nếu không tìm thấy theo paymentCode, thử tìm theo bookingCode
            payment = paymentRepository.findByBookingCode(payload.getReferenceCode()).orElse(null);
            if (payment != null) {
                log.info("Tìm thấy payment theo referenceCode (bookingCode)={} -> paymentId={}", payload.getReferenceCode(), payment.getId());
            } else {
                log.warn("Không tìm thấy payment theo referenceCode={} (cả paymentCode và bookingCode)", payload.getReferenceCode());
            }
        }
    }

    // 🔎 Nếu chưa thấy thì tìm theo bookingCode trong content
    if (payment == null && payload.getContent() != null) {
        String extracted = extractBookingCode(payload.getContent());
        log.info("Trích xuất từ content: '{}' -> {}", payload.getContent(), extracted);
        if (extracted != null) {
            payment = paymentRepository.findByBookingCode(extracted).orElse(null);
            log.info("Tìm theo bookingCode từ content={} -> payment={}", extracted, payment != null ? payment.getId() : "null");
        }
    }

    // 🔎 Nếu chưa thấy thì fallback sang description
    if (payment == null && payload.getDescription() != null) {
        String extracted = extractBookingCode(payload.getDescription());
        log.info("Trích xuất từ description: '{}' -> {}", payload.getDescription(), extracted);
        if (extracted != null) {
            payment = paymentRepository.findByBookingCode(extracted).orElse(null);
            log.info("Tìm theo bookingCode từ description={} -> payment={}", extracted, payment != null ? payment.getId() : "null");
        }
    }

    if (payment == null) {
        log.error("Không tìm thấy payment với: referenceCode={}, content={}, description={}", 
            payload.getReferenceCode(), payload.getContent(), payload.getDescription());
        throw new IllegalArgumentException("Không tìm thấy Payment theo referenceCode hoặc bookingCode");
    }

    Booking booking = payment.getBooking();
    log.info("Tìm thấy booking={} với trạng thái hiện tại: payment={}, booking={}", 
        booking.getBookingCode(), payment.getStatus(), booking.getStatus());

    String status = payload.getStatus() != null ? payload.getStatus().toUpperCase() : null;
    boolean success = "SUCCESS".equals(status) 
        || (status == null && "IN".equalsIgnoreCase(payload.getTransferType()));

    boolean amountMatched = payload.getAmount() != null && booking.getTotalAmount() != null
            && payload.getAmount().compareTo(booking.getTotalAmount()) == 0;

    log.info("Đánh giá webhook: success={}, amountMatched={}, expectedAmount={}, actualAmount={}", 
        success, amountMatched, booking.getTotalAmount(), payload.getAmount());

    if (success && amountMatched) {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(payload.getTransactionId());
        payment.setPaidAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.PAID);
        booking.setPayment(savedPayment);
        Booking savedBooking = bookingRepository.save(booking);

        try {
            emailService.sendBookingPaidEmail(savedBooking);
        } catch (Exception e) {
            log.error("Lỗi gửi email xác nhận thanh toán: {}", e.getMessage());
        }

        return savedBooking;

    } else if (!success) {
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        log.warn("Thanh toán thất bại cho bookingCode={} status={}", booking.getBookingCode(), payload.getStatus());
        return booking;

    } else {
        log.error("Số tiền không khớp cho bookingCode={} expected={} actual={}",
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
		
		// Tìm vị trí kết thúc của bookingCode (khi gặp ký tự không phải A-Z0-9)
		int start = idx;
		int end = start;
		while (end < text.length() && Character.isLetterOrDigit(text.charAt(end))) {
			end++;
		}
		
		String candidate = text.substring(start, end);
		log.info("Trích xuất bookingCode: '{}' -> '{}' (độ dài: {})", text, candidate, candidate.length());
		
		// Kiểm tra format: BK + 14 ký tự timestamp + 4 ký tự random = 20 ký tự
		if (candidate.length() >= 20 && candidate.startsWith("BK")) {
			return candidate;
		}
		
		log.warn("BookingCode không đúng format: '{}' (độ dài: {})", candidate, candidate.length());
		return null;
	}
}


