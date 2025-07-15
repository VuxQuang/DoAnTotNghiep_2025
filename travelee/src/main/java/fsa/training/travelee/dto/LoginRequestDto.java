package fsa.training.travelee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Sinh getter, setter, toString, equals, hashCode
@NoArgsConstructor // Constructor không tham số
@AllArgsConstructor // Constructor có tất cả tham số
public class LoginRequestDto {
    private String username;
    private String password;
}
