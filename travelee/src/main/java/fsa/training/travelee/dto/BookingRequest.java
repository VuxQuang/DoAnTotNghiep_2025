package fsa.training.travelee.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @NotNull(message = "Tour ID không được để trống")
    private Long tourId;

    @NotNull(message = "Schedule ID không được để trống")
    private Long scheduleId;

    @NotNull(message = "Ngày khởi hành không được để trống")
    private LocalDateTime departureDate;

    @NotNull(message = "Ngày về không được để trống")
    private LocalDateTime returnDate;

    @Min(value = 1, message = "Số người lớn phải ít nhất là 1")
    @Max(value = 50, message = "Số người lớn không được vượt quá 50")
    private Integer adultCount;

    @Min(value = 0, message = "Số trẻ em không được âm")
    @Max(value = 20, message = "Số trẻ em không được vượt quá 20")
    private Integer childCount;

    @Min(value = 0, message = "Số trẻ sơ sinh không được âm")
    @Max(value = 10, message = "Số trẻ sơ sinh không được vượt quá 10")
    private Integer infantCount;

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(min = 2, max = 100, message = "Tên khách hàng phải từ 2-100 ký tự")
    private String customerName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String customerEmail;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String customerAddress;

    @Size(max = 1000, message = "Yêu cầu đặc biệt không được vượt quá 1000 ký tự")
    private String specialRequests;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;
} 