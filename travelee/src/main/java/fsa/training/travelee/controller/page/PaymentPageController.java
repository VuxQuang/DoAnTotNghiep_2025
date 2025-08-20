package fsa.training.travelee.controller.page;

import fsa.training.travelee.entity.User;
import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.service.BookingService;
import fsa.training.travelee.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaymentPageController {

	private final BookingService bookingService;
	private final UserService userService;

	@Value("${sepay.bank-code:}")
	private String sepayBankCode; // Mã ngân hàng tài khoản mà SePay theo dõi

	@Value("${sepay.account-number:}")
	private String sepayAccountNumber; // Số tài khoản ngân hàng theo dõi bởi SePay

	@Value("${sepay.account-name:}")
	private String sepayAccountName; // Tên chủ tài khoản (hiển thị cho khách)

	@Value("${sepay.bank-name:}")
	private String sepayBankName; // Tên ngân hàng dạng hiển thị (ví dụ: MBBank)

	@Value("${sepay.qr.template-url:}")
	private String sepayQrTemplateUrl; // Mẫu URL ảnh QR của SePay (nếu có), hỗ trợ placeholder

	@GetMapping("/page/payment/{id}")
	public String showPaymentPage(@PathVariable Long id, Model model) {
		try {
			Booking booking = bookingService.getBookingById(id);
			if (booking == null) {
				return "redirect:/page/tours/newest";
			}

			User currentUser = userService.getCurrentUser();
			if (currentUser == null || !booking.getUser().getId().equals(currentUser.getId())) {
				return "redirect:/login";
			}

			model.addAttribute("booking", booking);
			model.addAttribute("user", currentUser);

			// Tính số tiền (toLong)
			long amount = toVndLong(booking.getTotalAmount());
			model.addAttribute("amountVnd", amount);

			// Tạo URL QR cho tài khoản SePay đang theo dõi
			String qrImageUrl = null;
			if (isConfigured(sepayBankCode) && isConfigured(sepayAccountNumber)) {
				String addInfo = booking.getBookingCode();
				if (isConfigured(sepayQrTemplateUrl)) {
					// Hỗ trợ các placeholder: {bankCode}, {accountNumber}, {accountName}, {amount}, {bookingCode}
					qrImageUrl = sepayQrTemplateUrl
							.replace("{bankCode}", urlEncode(sepayBankCode))
							.replace("{bankName}", urlEncode(isConfigured(sepayBankName) ? sepayBankName : sepayBankCode))
							.replace("{accountNumber}", urlEncode(sepayAccountNumber))
							.replace("{accountName}", urlEncode(sepayAccountName != null ? sepayAccountName : ""))
							.replace("{amount}", String.valueOf(amount))
							.replace("{bookingCode}", urlEncode(addInfo));
				} else {
					// Fallback: dùng QR mặc định của SePay theo mẫu công khai
					String bankParam = isConfigured(sepayBankName) ? sepayBankName : sepayBankCode;
					qrImageUrl = String.format(
							"https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%d&des=%s&template=compact",
							urlEncode(sepayAccountNumber), urlEncode(bankParam), amount, urlEncode(addInfo)
					);
				}
			}
			model.addAttribute("qrImageUrl", qrImageUrl);
			model.addAttribute("bankCode", sepayBankCode);
			model.addAttribute("accountNumber", sepayAccountNumber);
			model.addAttribute("accountName", sepayAccountName);

			return "page/booking/booking-payment";
		} catch (Exception e) {
			log.error("Lỗi hiển thị trang thanh toán: {}", e.getMessage());
			return "redirect:/page/tours/newest";
		}
	}

	@GetMapping("/page/payment/{id}/status")
	@ResponseBody
	public Map<String, Object> getPaymentStatus(@PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();
		try {
			Booking booking = bookingService.getBookingById(id);
			if (booking == null) {
				response.put("error", "NOT_FOUND");
				return response;
			}

			User currentUser = userService.getCurrentUser();
			if (currentUser == null || !booking.getUser().getId().equals(currentUser.getId())) {
				response.put("error", "UNAUTHORIZED");
				return response;
			}

			String paymentStatus = booking.getPayment() != null && booking.getPayment().getStatus() != null
					? booking.getPayment().getStatus().name()
					: "PENDING";
			String bookingStatus = booking.getStatus() != null ? booking.getStatus().name() : "PENDING";

			// Log để debug
			log.debug("Trạng thái thanh toán cho booking {}: paymentStatus={}, bookingStatus={}", 
				booking.getBookingCode(), paymentStatus, bookingStatus);

			response.put("paymentStatus", paymentStatus);
			response.put("bookingStatus", bookingStatus);
			response.put("bookingCode", booking.getBookingCode());
			response.put("redirectUrl", "/profile?tab=tours");
			
			// Thêm thông tin chi tiết để debug
			if (booking.getPayment() != null) {
				response.put("paymentId", booking.getPayment().getId());
				response.put("paymentAmount", booking.getPayment().getAmount());
				response.put("paymentCreatedAt", booking.getPayment().getCreatedAt());
				response.put("paymentPaidAt", booking.getPayment().getPaidAt());
			}
			
			return response;
		} catch (Exception e) {
			log.error("Lỗi khi lấy trạng thái thanh toán cho booking {}: {}", id, e.getMessage(), e);
			response.put("error", "INTERNAL_ERROR");
			return response;
		}
	}

	private boolean isConfigured(String value) {
		return value != null && !value.isBlank();
	}

	private long toVndLong(BigDecimal amount) {
		if (amount == null) return 0L;
		return amount.setScale(0, BigDecimal.ROUND_HALF_UP).longValueExact();
	}

	private String urlEncode(String value) {
		try {
			return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8.toString());
		} catch (Exception e) {
			return value;
		}
	}
}


