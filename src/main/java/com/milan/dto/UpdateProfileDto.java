package com.milan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UpdateProfileDto {

    @Size(min = 4, message = "First name cannot be empty")
    private String firstName;

    @Size(min = 4, message = "Last name cannot be empty")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String mobileNumber;

    private String address;

    private String city;

    private String state;

    private String zipCode;
}
