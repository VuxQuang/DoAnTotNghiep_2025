package fsa.training.travelee.dto.payment;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SepayWebhookDto {
	// Tùy theo SePay gửi gì, tạm thời dùng các trường phổ biến
	private String referenceCode; // bookingCode hoặc paymentCode ứng dụng gửi khi tạo QR/đơn
	@JsonAlias({"id", "transactionId"})
	private String transactionId; // mã giao dịch bên SePay
	private String status; // SUCCESS/FAILED/PENDING...
	@JsonAlias({"transferAmount", "amount"})
	private BigDecimal amount; // số tiền thực nhận

	@JsonAlias({"transferType"})
	private String transferType; // in / out
	private String signature; // chữ ký xác thực webhook (nếu có)

	// Các trường nội dung mô tả giao dịch để fallback trích mã booking
	@JsonAlias({"des", "description"})
	private String description;
	
	// Trường content riêng biệt (nếu có)
	@JsonAlias({"content"})
	private String content; 


}