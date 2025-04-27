package com.milan.controller;

import com.milan.dto.*;
import com.milan.dto.response.ImageResponse;
import com.milan.dto.response.PageableResponse;
import com.milan.model.SiteUser;
import com.milan.repository.UserRepository;
import com.milan.service.ImageService;
import com.milan.service.UserService;
import com.milan.util.CommonUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;

import static com.milan.util.MyConstants.*;

@RestController
@RequestMapping("/store/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final ImageService imageService;

    @Value("${image.user}")
    private String userImagePath;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    //display user profile details thats only accessible to the logged in user
    @GetMapping("/my-profile")
    public ResponseEntity<UserProfileResponseDto> getUserProfileDetails() {

        UserProfileResponseDto userProfile = userService.getCurrentUserProfileDetails();

        return ResponseEntity.ok(userProfile);
    }

    //Update user profile details
    @PutMapping("/profile-update")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileDto updateProfileDto) {

        userService.updateProfile(updateProfileDto);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()") // Any logged-in user
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequestDto request) {

        userService.changePassword(request);

        return CommonUtil.createBuildResponseMessage("Password changed successfully", HttpStatus.OK);
    }

    //get all the active users in pageable formats
    @GetMapping("/all-users")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> getAllUsers(
            @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "firstName") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        //get all users in pageable formats
        PageableResponse<UserProfileResponseDto> allUsers = userService.getAllUsers(pageNo, pageSize, sortBy, sortDir);

        //display all users response in pageable format
        return CommonUtil.createBuildResponse(allUsers, HttpStatus.OK);
    }

    //search?firstName=Milan&pageNumber=1&pageSize=10&sortBy=firstName&sortDir=asc - returns all user of that name
    //No Matching First Name	/search?firstName=InvalidName	Empty content array in response
    //Empty First Name Param	/search?firstName=	Returns all users (treated as null)
    //No Parameter	/search	Returns all users (paginated)
    @GetMapping("/search")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> searchUsers(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "pageNumber", defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "firstName") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        PageableResponse<UserProfileResponseDto> users = userService.searchUsers(firstName, pageNo, pageSize, sortBy, sortDir);

        return CommonUtil.createBuildResponse(users, HttpStatus.OK);
    }

    @PostMapping("/upload-profile-image")
    public ResponseEntity<?> uploadProductImage( @RequestParam("image") MultipartFile image) throws IOException {

        // Get the current user's ID securely
        SiteUser currentUser = CommonUtil.getLoggedInUser();
        Integer userId = currentUser.getId();

        // Upload the image and get the filename
        String imageName = imageService.uploadImage(image,userImagePath);

        // Update only the profile image of the user
        userService.updateProfileImage(userId, imageName);

        ImageResponse response = ImageResponse.builder()
                .imageName(imageName)
                .message("Image updated")
                .success(true)
                .status(HttpStatus.CREATED)
                .build();

        return CommonUtil.createBuildResponse(response,HttpStatus.CREATED);
    }

    @GetMapping("/profile-image")
    public void displayProfileImage(HttpServletResponse response) throws IOException {

        // 1. Get the current logged-in user
        SiteUser currentUser = CommonUtil.getLoggedInUser();
        Integer userId = currentUser.getId();

        // 2. Fetch user details from DB
        UserDto userDto = userService.getUserById(userId);

        // 3. Get the profile image name
        String imageName = userDto.getProfileImage();

        // 4. Load the image from the local folder
        InputStream resource = imageService.getResource(userImagePath, imageName);

        // Set content type based on file extension and stream the image
        if (imageName.endsWith(".png")) {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
        } else if (imageName.endsWith(".jpg") || imageName.endsWith(".jpeg")) {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        }

        StreamUtils.copy(resource, response.getOutputStream());
    }

    @DeleteMapping("/delete-profile-image")
    public ResponseEntity<?> deleteProfileImage() {

        // Get the current user
        SiteUser currentUser = CommonUtil.getLoggedInUser();
        Integer userId = currentUser.getId();

        // Delete the image and update the user's profileImage field
        userService.deleteProfileImage(userId);

        return CommonUtil.createBuildResponseMessage("Profile image deleted successfully", HttpStatus.OK);
    }

    @DeleteMapping("/account-delete")
    public ResponseEntity<?> deleteAccountDetails() {

        // Get the current Login user id
        Integer userId = CommonUtil.getLoggedInUser().getId();

        userService.deleteUser(userId);

        return CommonUtil.createBuildResponseMessage("Account deleted successfully", HttpStatus.OK);
    }

}
