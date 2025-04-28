package com.milan.service;

import com.milan.dto.ChangePasswordRequestDto;
import com.milan.dto.UpdateProfileDto;
import com.milan.dto.UserDto;
import com.milan.dto.UserProfileResponseDto;
import com.milan.dto.response.PageableResponse;
import com.milan.model.SiteUser;

public interface UserService {

    void updateProfile(UpdateProfileDto updateProfileDto);

    void updateProfileImage(Integer userId, String imageName);

    UserDto getUserById(Integer userId);

    void deleteUser(Integer userId);

    void deleteProfileImage(Integer userId);

    UserProfileResponseDto getCurrentUserProfileDetails();

    PageableResponse<UserProfileResponseDto> getAllUsers(int pageNo, int pageSize, String sortBy, String sortDir);

    PageableResponse<UserProfileResponseDto> searchUsers(String firstName,int pageNo, int pageSize, String sortBy, String sortDir);

    void changePassword(ChangePasswordRequestDto request);

    SiteUser getUserByEmail(String email);

}
