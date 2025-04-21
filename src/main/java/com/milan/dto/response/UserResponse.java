package com.milan.dto.response;

import com.milan.dto.AccountStatusDto;
import com.milan.dto.RoleDto;
import com.milan.dto.UserDto;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
//Removed password for response
public class UserResponse {

    private Integer id;

    private String firstName;

    private String lastName;

    private String email;

    private String profileImage;

    private String mobileNumber;

    private String address;

    private String city;

    private String state;

    private String zipCode;

    private Set<RoleDto> roles = new HashSet<>();

    private AccountStatusDto status;

}
