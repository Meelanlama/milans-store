package com.milan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDto {

    private Integer id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String profileImage;

    private String mobileNumber;

    private String address;

    private String city;

    private String state;

    private String zipCode;

    private Set<RoleDto> roles = new HashSet<>();
    
}
