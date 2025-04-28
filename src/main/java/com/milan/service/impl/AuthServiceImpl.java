package com.milan.service.impl;

import com.milan.dto.ResetPasswordDto;
import com.milan.dto.RoleDto;
import com.milan.dto.UserDto;
import com.milan.dto.request.EmailRequest;
import com.milan.dto.request.LoginRequest;
import com.milan.dto.response.LoginResponse;
import com.milan.dto.response.UserResponse;
import com.milan.exception.ResourceNotFoundException;
import com.milan.model.AccountStatus;
import com.milan.model.Role;
import com.milan.model.SiteUser;
import com.milan.repository.RoleRepository;
import com.milan.repository.UserRepository;
import com.milan.security.CustomUserDetails;
import com.milan.security.JwtService;
import com.milan.service.AuthService;
import com.milan.service.UserService;
import com.milan.util.CheckValidation;
import com.milan.util.CommonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;

    private final UserRepository userRepo;

    private final ModelMapper mapper;

    private final CheckValidation checkValidation;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepo;

    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;

    private final UserDetailsService userDetailsService;

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public Boolean registerUser(UserDto userDto,String url) throws Exception {

        logger.info("Starting registration process for email={}", userDto.getEmail());

        //validate details before register
        checkValidation.userValidation(userDto);

        SiteUser saveUser = mapper.map(userDto, SiteUser.class);

        setRole(userDto,saveUser);

        //Set account status false and generate token As, they need to verify their email address
        //Create new account status for the new user as they don't have it yet
        AccountStatus status = AccountStatus.builder()
                .isAccountActive(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24)) // Set expiry of token to 24 hours from now
                .build();

        saveUser.setAccountStatus(status);
        saveUser.setPassword(passwordEncoder.encode(saveUser.getPassword()));

        logger.info("User object prepared for saving: {}", saveUser);

        try{
            // Save the user and proceed
            SiteUser savedUser = userRepo.save(saveUser);
            logger.info("User registration successful for email={}", savedUser.getEmail());
            //send email after register
            emailSend(savedUser, url);
            return true; // Return true indicating success
        } catch (Exception e) {
            // If there's an exception, log the error
            logger.error("User registration failed for email={}", userDto.getEmail(), e);
            return false; // Return false indicating failure
        }
    }


    private void setRole(UserDto userDto, SiteUser saveUser) {
        // Extract role IDs from the UserDto object
        List<Integer> roleId = userDto.getRoles()
                .stream()
                .map(RoleDto::getId)
                .toList();

        // Fetch Role entities from the database based on the extracted role IDs
        List<Role> roles = roleRepo.findAllById(roleId);

        // Assign the fetched roles to the SiteUser entity
        // Convert the list into a set (as setRoles expects a Set)
        saveUser.setRoles(new HashSet<>(roles));
    }

    private void emailSend(SiteUser savedUser, String url) throws Exception {

        // Build the verification URL with proper query parameter formatting and URL encoding
        //Account verification done is verification service
        String verificationToken = URLEncoder.encode(savedUser.getAccountStatus().getVerificationToken(), StandardCharsets.UTF_8.toString());
        String verificationUrl = url + "/store/v1/account-verify/verify-register?userId=" + savedUser.getId() + "&verificationToken=" + verificationToken;

        try {
            String emailMessage = "Hello, <b>[[username]]</b><br>"
                    + "<br> Your account has been registered successfully in our website.<br>"
                    + "<br> Now, Please Click the link below to verify & Activate your account.<br>"
                    + "<a href ='[[url]]'>Click Here</a> <br><br>"
                    + "Thanks,<br>Milan's Online Store Team";

            emailMessage = emailMessage.replace("[[username]]", savedUser.getFirstName());
            emailMessage = emailMessage.replace("[[url]]", verificationUrl);

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(savedUser.getEmail())
                    .title("Confirm Your Registration")
                    .subject("Account Register Successful")
                    .message(emailMessage)
                    .build();

            logger.info("Sending email to: {}", savedUser.getEmail());
            emailService.sendEmail(emailRequest);

        } catch (Exception e) {
            logger.error("Failed to send email for user {}: {}", savedUser.getEmail(), e.getMessage(), e);
        }
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        logger.info("Login attempt for email={}", loginRequest.getEmail());

        try{
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                    (loginRequest.getEmail(), loginRequest.getPassword()));


            if(authenticate.isAuthenticated()){
                CustomUserDetails customUserDetails = (CustomUserDetails) authenticate.getPrincipal();
                SiteUser user = customUserDetails.getSiteUser();

                // Check if account is active or not
                if (user.getAccountStatus() == null || !Boolean.TRUE.equals(user.getAccountStatus().getIsAccountActive())) {
                    logger.warn("Login failed: Account not active for email={}", loginRequest.getEmail());
                    throw new RuntimeException("Account not activated. Please verify your email and try again. " +
                            "If you have already verified your email, please contact us for assistance.");
                }

                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                LoginResponse loginResponse = LoginResponse.builder()
                        .user(mapper.map(customUserDetails.getSiteUser(), UserResponse.class))
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

                logger.info("Login successful for email={}", loginRequest.getEmail());
                return loginResponse;
            }else {
                logger.warn("Login failed: Invalid credentials for email={}", loginRequest.getEmail());
            }
        }catch (Exception e) {
            logger.error("Error during login for email={}: {}", loginRequest.getEmail(), e.getMessage(), e);
        }
        return null;
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {

        // Extract the username (email) from the refresh token
        String username = jwtService.extractUsername(refreshToken);

        // Retrieve the user from the database
        SiteUser user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Load the UserDetails object for token validation
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        boolean isValid = jwtService.validateToken(refreshToken, userDetails);

        // Validate the refresh token using the UserDetails object
        if (!isValid) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // Generate a new access token and optional new refresh token (token rotation)
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user); // Optional rotation

        // Return the response with the new tokens
        return LoginResponse.builder()
                .user(mapper.map(user, UserResponse.class))
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void processForgotPassword(String email, HttpServletRequest request) {

        SiteUser userByEmail = userService.getUserByEmail(email);

        //SET password reset token in account status
        //Don't create a new account status for the user as use the one linked to it.
        //if directly created without checking null it will always create new account status for the account
        // Get the existing AccountStatus, or create a new one if it doesn't exist
        AccountStatus accountStatus = userByEmail.getAccountStatus();

        // If the AccountStatus is null, then only create a new one (only if required)
        if (accountStatus == null) {
            accountStatus = new AccountStatus();
            accountStatus.setIsAccountActive(false);
        }

        // Generate a reset token and update the password reset token field
        String passwordResetToken = UUID.randomUUID().toString();
        accountStatus.setPasswordResetToken(passwordResetToken);

        // Set expiry time for the password reset token
        accountStatus.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(30)); // 30 minutes expiry

        // Update the account status of the user
        userByEmail.setAccountStatus(accountStatus);

        // Save the user with the updated account status (this will not change the account_status_id as we're using the linked to it)
        userRepo.save(userByEmail);

        //get the base url to modify if dynamically: right now: localhost:8080/
        String baseUrl = CommonUtil.getUrl(request);

        try {
            //send email with the url(api) and token
            sendResetPasswordEmail(userByEmail, baseUrl);
        } catch (Exception e) {
            logger.error("Failed to send password reset email", e);
            throw new RuntimeException("Failed to send reset password email");
        }
        logger.info("Password reset email sent successfully for email={}", email);
    }

    @Override
    public boolean validateResetPasswordToken(String token) {

        // Find the user by the password reset token
        SiteUser user = userRepo.findByAccountStatusPasswordResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid password reset token"));

        // Check if the account is active
        if (!user.getAccountStatus().getIsAccountActive()) {
            throw new InvalidOperationException("Account is not active.");
        }

        //check if token is null
        if (user.getAccountStatus().getPasswordResetToken() == null) {
            throw new InvalidOperationException("You have already used this token to reset password. Please request a new one again.");
        }

        // Check if token time has expired. if user try to access token after more than 30 min. invalidate it
        LocalDateTime tokenExpiry = user.getAccountStatus().getPasswordResetTokenExpiry();
        if (tokenExpiry != null && LocalDateTime.now().isAfter(tokenExpiry)) {
            throw new InvalidOperationException("Password reset token has expired. Please request a new one.");
        }

        return true;  // Return true if the token is valid

    }

    @Override
    public void resetPassword(String token, ResetPasswordDto resetPasswordDto) {

        // Retrieve the user associated with the given reset token.
        SiteUser user = userRepo.findByAccountStatusPasswordResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired token"));

        // Validate that new password and confirm password match for reset password
        if (!resetPasswordDto.getNewPassword().equals(resetPasswordDto.getConfirmNewPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        // Encode the new password
        String encodedPassword = passwordEncoder.encode(resetPasswordDto.getNewPassword());
        user.setPassword(encodedPassword);

        // Clear the reset token after successful password change for that user
        user.getAccountStatus().setPasswordResetToken(null);
        user.getAccountStatus().setPasswordResetTokenExpiry(null);

        // Save the updated user with new password
        userRepo.save(user);

        logger.info("Password reset successfully for userId={}", user.getId());

    }

    private void sendResetPasswordEmail(SiteUser user, String url) throws Exception {

        // Properly encode the reset token
        String resetToken = URLEncoder.encode(user.getAccountStatus().getPasswordResetToken(), StandardCharsets.UTF_8.toString());

        //call that reset password api with the help of our base url
        String resetPasswordUrl = url + "/store/v1/auth/reset-password?token=" + resetToken;

        try {
            String emailMessage = "Hello, <b>[[username]]</b><br>"
                    + "<br> You have requested to reset your password.<br>"
                    + "<br> Please Click the link below to reset your password:<br>"
                    + "<a href ='[[url]]'>Reset Password</a> <br><br>"
                    + "If you did not request this, Please ignore this email.<br><br>"
                    + "Thanks,<br>Milan's Online Store Team";

            emailMessage = emailMessage.replace("[[username]]", user.getFirstName());
            emailMessage = emailMessage.replace("[[url]]", resetPasswordUrl);

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(user.getEmail())
                    .title("Password Reset Request")
                    .subject("Reset Your Password")
                    .message(emailMessage)
                    .build();

            logger.info("Sending password reset email to: {}", user.getEmail());
            emailService.sendEmail(emailRequest);

        } catch (Exception e) {
            logger.error("Failed to send password reset email for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }


}
