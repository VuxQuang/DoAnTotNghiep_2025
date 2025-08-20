package fsa.training.travelee.dto.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRequestDto {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Ngày sinh không được để trống")
    private String dateOfBirth;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "Giới tính phải là MALE hoặc FEMALE")
    private String gender;

    @NotBlank(message = "CMND/CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{9,12}$", message = "CMND/CCCD không hợp lệ")
    private String idCard;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotBlank(message = "Loại người tham gia không được để trống")
    @Pattern(regexp = "^(ADULT|CHILD)$", message = "Loại phải là ADULT hoặc CHILD")
    private String type;
}

