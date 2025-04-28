package com.milan.Service;

import com.milan.dto.ChangePasswordRequestDto;
import com.milan.model.SiteUser;
import com.milan.repository.UserRepository;
import com.milan.security.CustomUserDetails;
import com.milan.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    // Mock database interactions (we don't test real DB here)
    @Mock
    private UserRepository userRepository;

    // Mock password encoder (we'll control its behavior)
    @Mock
    private PasswordEncoder passwordEncoder;

    // Create service instance with our mocks
    @InjectMocks
    private UserServiceImpl userService;

    private SiteUser testUser;

    private final BCryptPasswordEncoder realEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // 1. Create test user with initial email and password
        testUser = new SiteUser();
        testUser.setEmail("test@example.com");
        testUser.setPassword(realEncoder.encode("oldPassword123!"));

        // 2. Fake user login session
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities() );// Crucial for authenticated status

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

   // This tests the password change feature
   @DisplayName("Change Password Test Method")
   @Test
    void changePassword_ValidRequest_UpdatesPassword() {

        // Arrange
        // Prepare test data
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setCurrentPassword("oldPassword123!");
        request.setNewPassword("newSecurePassword456@");
        request.setConfirmNewPassword("newSecurePassword456@");

        // Use REAL password encoder to verify if old password matches the one in database
        //eq checks if it they are equal
        //Alternatives: any() → matches any value
        //isNull() → matches null
        //same() → checks object identity
        when(passwordEncoder.matches(eq("oldPassword123!"), eq(testUser.getPassword()))).thenReturn(true);

        // Use REAL password encoder for new password to encode it and return it
        String newEncodedPassword = realEncoder.encode("newSecurePassword456@");
        when(passwordEncoder.encode(anyString())).thenReturn(newEncodedPassword);

        // Act
        // Execute password change
        userService.changePassword(request);

        // Assert
        // Verify two things happened:
        // 1. Password was updated to new one
        assertTrue(realEncoder.matches("newSecurePassword456@", testUser.getPassword()));

        // 2. User was saved to database
        assertEquals(newEncodedPassword, testUser.getPassword());
    }

}
