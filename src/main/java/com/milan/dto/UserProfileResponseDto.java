package com.milan.dto;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private String address;

    private String city;

    private String state;

    private String zipCode;

    // full URL like /profile-image
    private String profileImage;

    private Set<RoleDto> roles = new HashSet<>();

}


