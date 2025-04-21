package com.milan.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginResponse {

    private UserResponse user;

    private String accessToken;

    private String refreshToken;

}
