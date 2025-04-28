package com.milan.service;

import com.milan.dto.ResetPasswordDto;
import com.milan.dto.UserDto;
import com.milan.dto.request.LoginRequest;
import com.milan.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

public interface AuthService {

    Boolean registerUser(UserDto userDto,String url) throws Exception;

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshToken(String refreshToken);

    void processForgotPassword(String email, HttpServletRequest request);

    boolean validateResetPasswordToken(String token);

    void resetPassword(String token, @Valid ResetPasswordDto resetPasswordDto);

}