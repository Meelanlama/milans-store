package com.milan.util;

import com.milan.dto.CategoryDto;
import com.milan.dto.UserDto;
import com.milan.exception.ExistDataException;
import com.milan.exception.MyValidationException;
import com.milan.repository.RoleRepository;
import com.milan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CheckValidation {

    private final RoleRepository roleRepo;

    private final UserRepository userRepo;

    //Validation for user register
    public void userValidation(UserDto userDto) {

        if (userDto == null) {
            throw new IllegalArgumentException("Please enter all details while registering");
        }

        // First Name
        if (!StringUtils.hasText(userDto.getFirstName())) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (userDto.getFirstName().length() > MyConstants.FIRST_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("First name cannot exceed " + MyConstants.FIRST_NAME_MAX_LENGTH + " characters.");
        }

        // Last Name
        if (!StringUtils.hasText(userDto.getLastName())) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (userDto.getLastName().length() > MyConstants.LAST_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Last name cannot exceed " + MyConstants.LAST_NAME_MAX_LENGTH + " characters.");
        }

        //Email
        if(!StringUtils.hasText(userDto.getEmail()) || !userDto.getEmail().matches(MyConstants.EMAIL_REGEX)) {
            throw new IllegalArgumentException("Please enter valid email");
        }else {
            //validate duplicate email
            boolean exist = userRepo.existsByEmail(userDto.getEmail());
            if(exist) {
                throw new ExistDataException("Email already exists");
            }
        }

        //Mobile Number
        if(!StringUtils.hasText(userDto.getMobileNumber()) || !userDto.getMobileNumber().matches(MyConstants.MOBILE_REGEX)) {
            throw new IllegalArgumentException("Please enter valid mobile number");
        }

        //Password
        if(StringUtils.hasText(userDto.getPassword()) && userDto.getPassword().length() < MyConstants.PASSWORD_MIN_LENGTH) {
            throw new IllegalArgumentException("Please enter valid password");
        }

        //Zip Code
        if(StringUtils.hasText(userDto.getZipCode()) && userDto.getZipCode().length() < 5 && !userDto.getZipCode().matches(MyConstants.ZIP_CODE_REGEX)) {
            throw new IllegalArgumentException("Please enter valid zip code");
        }

        //City
        if(!StringUtils.hasText(userDto.getCity())) {
            throw new IllegalArgumentException("Please enter valid city");
        }

        //State
        if(!StringUtils.hasText(userDto.getState())) {
            throw new IllegalArgumentException("Please enter valid zip code");
        }

        //Role
        //Check if the user DTO contains any roles at all.
        if(CollectionUtils.isEmpty(userDto.getRoles())) {
            throw new IllegalArgumentException("Please enter valid role. It's Empty");
        }else {
            // Roles were provided, now check if they are valid roles stored in the database.
            // Get a list of all valid Role IDs that exist in the database.
            List<Integer> allRoleIds = roleRepo.findAll()
                    .stream()
                    .map(r -> r.getId()).
                    collect(Collectors.toList());

            //Filter the roles requested in the DTO to keep only the ones that exist in allRoleIds.
            List<Integer> roleRequestId = userDto.getRoles().stream()
                    .map(r -> r.getId())
                    .filter(roleId -> allRoleIds.contains(roleId)).toList();

            //Check if any of the roles provided in the DTO were valid.
            if(CollectionUtils.isEmpty(roleRequestId)) {
                throw new IllegalArgumentException("Please enter valid role:" + roleRequestId);
            }
        }
    }


    //Category Validation
    public void categoryValidation(CategoryDto categoryDto) {

        Map<String, Object> error = new LinkedHashMap<>();

        if (ObjectUtils.isEmpty(categoryDto)) {
            throw new IllegalArgumentException("category Object/JSON shouldn't be null or empty");
        } else {

            // validation name field
            if (ObjectUtils.isEmpty(categoryDto.getCategoryName()) || !StringUtils.hasText(categoryDto.getCategoryName())) {
                error.put("Name", "Name field is empty or null");
            } else {
                if (categoryDto.getCategoryName().length() < 3) {
                    error.put("Name", "Name length min 3");
                }
                if (categoryDto.getCategoryName().length() > 100) {
                    error.put("Name", "Name length max 100");
                }
            }

            // validation description
            if (ObjectUtils.isEmpty(categoryDto.getDescription())) {
                error.put("Description", "Description field is empty or null");
            }

            // validation isActive
            if (ObjectUtils.isEmpty(categoryDto.getIsActive())) {
                error.put("isActive", "isActive field is empty or null");
            } else {
                //means, it should be true or false only, as we're soft deleting it and showing based on T/F value
                if (categoryDto.getIsActive() != Boolean.TRUE.booleanValue()
                        && categoryDto.getIsActive() != Boolean.FALSE.booleanValue()) {
                    error.put("isActive", "Tried to add Invalid value in isActive field ");
                }
            }
        }

        if (!error.isEmpty()) {
            throw new MyValidationException(error);
        }
    }

}
