package com.milan.controller;

import com.milan.dto.ResetPasswordDto;
import com.milan.dto.UserDto;
import com.milan.dto.request.LoginRequest;
import com.milan.dto.request.RefreshTokenRequest;
import com.milan.dto.response.LoginResponse;
import com.milan.model.SiteUser;
import com.milan.service.AuthService;
import com.milan.service.UserService;
import com.milan.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

@RestController
@RequestMapping("/store/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto, HttpServletRequest request) throws Exception {

        //get that dynamic url for email verification
        String url = CommonUtil.getUrl(request);

        logger.info("Registering new user: email={}, url={}", userDto.getEmail(), url);

        Boolean register = authService.registerUser(userDto, url);

        if (register) {
            logger.info("User registration successful for email={}", userDto.getEmail());
            return CommonUtil.createBuildResponseMessage("Register success", HttpStatus.CREATED);
        }

        logger.error("User registration failed for email={}", userDto.getEmail());
        return CommonUtil.createErrorResponseMessage("Register failed", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws Exception{

        logger.info("Login attempt for email={}", loginRequest.getEmail());

        LoginResponse loginResponse = authService.login(loginRequest);

        if (ObjectUtils.isEmpty(loginResponse)) {
            logger.warn("Login failed: Invalid credentials for email={}", loginRequest.getEmail());
            return CommonUtil.createErrorResponseMessage("Invalid credential", HttpStatus.BAD_REQUEST);
        }

        logger.info("Login successful for email={}", loginRequest.getEmail());
        return CommonUtil.createBuildResponse(loginResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse loginResponse = authService.refreshToken(request.getRefreshToken());
            return CommonUtil.createBuildResponse(loginResponse, HttpStatus.OK);
        } catch (Exception e) {
            return CommonUtil.createErrorResponseMessage(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> processForgetPassword(@RequestParam String email, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {

        logger.info("Processing forget password request for email={}", email);

        // Call the service to handle forgot password logic (generate token, send reset-link)
        authService.processForgotPassword(email,request);

        return CommonUtil.createBuildResponseMessage("Password reset link sent successfully", HttpStatus.OK);

    }

    // GET endpoint to validate a password reset token
    // This endpoint is called when a user clicks on a reset link in their email
    @GetMapping("/reset-password")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        logger.info("Validating reset password token={}", token);

        // Call service to validate the token
        authService.validateResetPasswordToken(token);

        // If valid, show the form (or respond that it's ready for password reset)
        return CommonUtil.createBuildResponseMessage("Token is valid, Please reset your password", HttpStatus.OK);
    }

    // POST request to handle resetting the password
    // The token is received as a query parameter and the new password is provided in the request body
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @Valid @RequestBody ResetPasswordDto resetPasswordDto) {

        logger.info("Resetting password with token={}", token);

        authService.resetPassword(token,resetPasswordDto);

        return CommonUtil.createBuildResponseMessage("Password reset successfully", HttpStatus.OK);
    }

}
