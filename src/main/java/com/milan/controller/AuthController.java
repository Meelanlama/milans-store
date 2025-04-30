package com.milan.controller;

import com.milan.dto.ResetPasswordDto;
import com.milan.dto.UserDto;
import com.milan.dto.request.LoginRequest;
import com.milan.dto.request.RefreshTokenRequest;
import com.milan.dto.response.LoginResponse;
import com.milan.exception.ResourceNotFoundException;
import com.milan.model.SiteUser;
import com.milan.service.AuthService;
import com.milan.service.UserService;
import com.milan.util.CommonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

@Tag(name = "AUTHENTICATION", description = "API for Register & Login")
@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",description = "Register successful"),
            @ApiResponse(responseCode = "500",description = "Internal server error"),
            @ApiResponse(responseCode = "400",description = "Bad Request")})
    @Operation(summary = "Register User API")
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

    //VERIFY THE ACCOUNT WITH THE LINK IN EMAIL
    @Operation(summary = "Verify New Register User Via Email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Verify successful")})
    @GetMapping("/verify-register")
    public ResponseEntity<?> verifyUserAccount(@RequestParam Integer userId, @RequestParam String verificationToken) {
        logger.info("Verifying user account for userId={} with verification token={}", userId, verificationToken);

        try {
            authService.verifyRegisterAccount(userId, verificationToken);
            return CommonUtil.createBuildResponse("Account verification successful", HttpStatus.OK);
        } catch (InvalidOperationException e) {
            logger.warn("Account verification failed for userId={}: {}", userId, e.getMessage());
            return CommonUtil.createErrorResponseMessage(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Create a new user", description = "Saves user data")
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

    @Operation(summary = "Refresh access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse loginResponse = authService.refreshToken(request.getRefreshToken());
            return CommonUtil.createBuildResponse(loginResponse, HttpStatus.OK);
        } catch (Exception e) {
            return CommonUtil.createErrorResponseMessage(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "Send reset password link to email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset link sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email or failure sending email")
    })
    @PostMapping("/forget-password")
    public ResponseEntity<?> processForgetPassword(@RequestParam String email, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {

        logger.info("Processing forget password request for email={}", email);

        // Call the service to handle forgot password logic (generate token, send reset-link)
        authService.processForgotPassword(email,request);

        return CommonUtil.createBuildResponseMessage("Password reset link sent successfully", HttpStatus.OK);

    }

    @Operation(summary = "Validate password reset token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
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

    @Operation(summary = "Reset password using valid token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token or password criteria not met")
    })
    // POST request to handle resetting the password
    // The token is received as a query parameter and the new password is provided in the request body
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @Valid @RequestBody ResetPasswordDto resetPasswordDto) {

        logger.info("Resetting password with token={}", token);

        authService.resetPassword(token,resetPasswordDto);

        return CommonUtil.createBuildResponseMessage("Password reset successfully", HttpStatus.OK);
    }

}
