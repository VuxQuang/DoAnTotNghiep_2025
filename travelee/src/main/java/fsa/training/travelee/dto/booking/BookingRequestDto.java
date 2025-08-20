package fsa.training.travelee.dto.booking;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    @NotNull(message = "Tour ID không được để trống")
    private Long tourId;

    @NotNull(message = "Schedule ID không được để trống")
    private Long scheduleId;

    @NotNull(message = "Số người lớn không được để trống")
    @Min(value = 1, message = "Số người lớn phải từ 1 trở lên")
    @Max(value = 50, message = "Số người lớn không được vượt quá 50")
    private Integer adultCount;

    @NotNull(message = "Số trẻ em không được để trống")
    @Min(value = 0, message = "Số trẻ em không được âm")
    @Max(value = 20, message = "Số trẻ em không được vượt quá 20")
    private Integer childCount;

    @NotBlank(message = "Họ tên người đặt không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String customerName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String customerEmail;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String specialRequests;

    @NotEmpty(message = "Danh sách người tham gia không được để trống")
    @Size(min = 1, max = 50, message = "Số người tham gia phải từ 1 đến 50")
    private List<ParticipantRequestDto> participants;
}
