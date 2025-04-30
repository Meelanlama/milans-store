package com.milan.service.impl;

import com.milan.dto.ChangePasswordRequestDto;
import com.milan.dto.UpdateProfileDto;
import com.milan.dto.UserDto;
import com.milan.dto.UserProfileResponseDto;
import com.milan.handler.PageableResponse;
import com.milan.exception.ResourceNotFoundException;
import com.milan.handler.PageMapper;
import com.milan.model.SiteUser;
import com.milan.repository.UserRepository;
import com.milan.service.UserService;
import com.milan.util.CommonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepo;

    private final ModelMapper mapper;

    @Value("${image.user}")
    private String userImagePath;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void updateProfile(UpdateProfileDto updateProfileDto) {

        try{
            //GET CURRENT LOGGED IN USER
            SiteUser currentUser  = CommonUtil.getLoggedInUser();

            //update profile, these fields are only applicable for update
            Optional.ofNullable(updateProfileDto.getFirstName()).ifPresent(currentUser::setFirstName);
            Optional.ofNullable(updateProfileDto.getLastName()).ifPresent(currentUser::setLastName);
            Optional.ofNullable(updateProfileDto.getMobileNumber()).ifPresent(currentUser::setMobileNumber);
            Optional.ofNullable(updateProfileDto.getAddress()).ifPresent(currentUser::setAddress);
            Optional.ofNullable(updateProfileDto.getCity()).ifPresent(currentUser::setCity);
            Optional.ofNullable(updateProfileDto.getState()).ifPresent(currentUser::setState);
            Optional.ofNullable(updateProfileDto.getZipCode()).ifPresent(currentUser::setZipCode);

            userRepo.save(currentUser);
        } catch (Exception e) {
            logger.error("Failed to update profile: ", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void updateProfileImage(Integer userId, String imageName) {
        try {
            SiteUser user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + userId + " or user is not active or deleted. " +
                            "Please try again with valid user ID or contact with admin if issue persists."));

            // Delete the old image before updating to the new one
            deleteProfileImageSafely(user); //

            user.setProfileImage(imageName);

            userRepo.save(user);

        } catch (Exception e) {
            logger.error("Failed to update profile image for user ID " + userId, e);
            throw e;
        }
    }

    @Override
    public UserDto getUserById(Integer userId) {

        SiteUser user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + userId));

        logger.info("User found with ID: {}", userId);

        return mapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {

        // Fetch the user from the database
        SiteUser user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + userId));

        // Validate ownership using security context
        Integer currentUserId = CommonUtil.getLoggedInUser().getId();
        if (!user.getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own account");
        }

        // Delete profile image (if exists)
        deleteProfileImageSafely(user);

        // Delete user account
        userRepo.delete(user);
        logger.info("User {} deleted successfully", userId);
    }

    @Override
    @Transactional
    public void deleteProfileImage(Integer userId) {
        try {
            // Fetch the user
            SiteUser user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + userId));

            // Delete the image file from storage
            deleteProfileImageSafely(user);

            // Clear the profileImage field in the database
            // If profileImage is null, show a default image in frontend
            user.setProfileImage(null);
            userRepo.save(user);

        } catch (Exception e) {
            logger.error("Failed to delete profile image for user ID " + userId, e);
            throw e;
        }
    }

    public UserProfileResponseDto getCurrentUserProfileDetails() {

        SiteUser currentUser = CommonUtil.getLoggedInUser();

        // Mapping directly from entity to response DTO
        UserProfileResponseDto profileDto = mapper.map(currentUser, UserProfileResponseDto.class);

        // Handle profile image URL
        if (currentUser.getProfileImage() != null) {

            //automatically change url: for now its localhost:8080
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

            //get the profile image of user by calling this method in our controller
            profileDto.setProfileImage(baseUrl + "/store/v1/users/profile-image");
        } else {
            profileDto.setProfileImage(null);
        }

        return profileDto;
    }

    @Override
    public PageableResponse<UserProfileResponseDto> getAllUsers(int pageNo, int pageSize, String sortBy, String sortDir) {

        Sort sort = (sortDir.equalsIgnoreCase("asc")) ? (Sort.by(sortBy).ascending()) : (Sort.by(sortBy).descending());

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        //find only users
        Page<SiteUser> page = userRepo.findByRoles_Name("USER",pageable);

        return PageMapper.getPageableResponse(page, UserProfileResponseDto.class);

    }


    @Override
    public PageableResponse<UserProfileResponseDto> searchUsers(String firstName, int pageNo, int pageSize, String sortBy, String sortDir) {

        firstName = StringUtils.hasText(firstName) ? firstName.trim() : null;

        //Validate sortBy to prevent SQL injection or invalid fields:
        List<String> allowedSortFields = Arrays.asList("firstName", "email", "createdAt");
        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "firstName"; // Default to a safe field
        }

        // Adjust for 1-based page number from frontend
        //int adjustedPageNo = pageNo > 0 ? pageNo - 1 : 0;

        Sort sort = (sortDir.equalsIgnoreCase("asc")) ? (Sort.by(sortBy).ascending()) : (Sort.by(sortBy).descending());

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Querying in the database directly with the repository
        Page<SiteUser> usersPage = userRepo.searchUsers(firstName, pageable);

        return PageMapper.getPageableResponse(usersPage, UserProfileResponseDto.class);
    }

    @Override
    public void changePassword(ChangePasswordRequestDto request) {
        // Get current user securely
        SiteUser user = CommonUtil.getLoggedInUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        // Check new password uniqueness
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current");
        }

        // Update password of the user
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
    }

    @Override
    public SiteUser getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }

    //This method for deleting it from our local storage folder
    private void deleteProfileImageSafely(SiteUser user) {

        //if no image is found return
        if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
            logger.info("No profile image to delete for user {}", user.getId());
            return;
        }

        //DELETE IMAGE FROM THE PATH
        Path imagePath = Paths.get(userImagePath + user.getProfileImage());

        try {
            if (Files.deleteIfExists(imagePath)) {
                logger.info("Deleted profile image: {}", imagePath);
            } else {
                logger.info("Profile image not found: {}", imagePath);
            }
        } catch (IOException e) {
            logger.error("Failed to delete image {}: {}", imagePath, e.getMessage());
        }
    }

}
