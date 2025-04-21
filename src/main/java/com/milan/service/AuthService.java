package com.milan.service;

import com.milan.dto.UserDto;
import com.milan.dto.request.LoginRequest;
import com.milan.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

public interface AuthService {

    Boolean registerUser(UserDto userDto,String url) throws Exception;

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshToken(String refreshToken);
}
