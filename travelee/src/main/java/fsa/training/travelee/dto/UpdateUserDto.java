package fsa.training.travelee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
}
