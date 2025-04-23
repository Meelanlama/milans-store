package com.milan.util;

import com.milan.dto.AddCartItems;
import com.milan.dto.CategoryDto;
import com.milan.dto.ProductDto;
import com.milan.dto.UserDto;
import com.milan.exception.ExistDataException;
import com.milan.exception.MyValidationException;
import com.milan.model.Product;
import com.milan.model.SiteUser;
import com.milan.repository.ProductRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CheckValidation {

    private final RoleRepository roleRepo;

    private final UserRepository userRepo;

    private final ProductRepository productRepo;

    //USER REGISTER VALIDATION
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

        //address
        if(!StringUtils.hasText(userDto.getAddress())) {
            throw new IllegalArgumentException("Please enter valid address. It's Empty");
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

    //---------------------------------------------------------------------------------------------------------------------------

    //CATEGORY VALIDATION
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

    //---------------------------------------------------------------------------------------------------------------------------

    //PRODUCT VALIDATION
    public void productValidation(ProductDto productDto) {
        Map<String, Object> error = new LinkedHashMap<>();

        if (ObjectUtils.isEmpty(productDto)) {
            throw new IllegalArgumentException("product Object/JSON shouldn't be null or empty");
        } else {
            // Validate product name
            if (ObjectUtils.isEmpty(productDto.getProductName()) || !StringUtils.hasText(productDto.getProductName())) {
                error.put("ProductName", "Product name field is empty or null");
            } else {
                if (productDto.getProductName().length() < 3) {
                    error.put("ProductName", "Product name length min 3");
                }
                if (productDto.getProductName().length() > 100) {
                    error.put("ProductName", "Product name length max 100");
                }
            }

            // Validate short description
            if (ObjectUtils.isEmpty(productDto.getShortDescription()) || !StringUtils.hasText(productDto.getShortDescription())) {
                error.put("ShortDescription", "Short description field is empty or null");
            } else {
                if (productDto.getShortDescription().length() > 200) {
                    error.put("ShortDescription", "Short description length max 200");
                }
            }

            // Validate full description
            if (ObjectUtils.isEmpty(productDto.getDescription())) {
                error.put("Description", "Description field is empty or null");
            }

            // Validate unit price
            if (ObjectUtils.isEmpty(productDto.getUnitPrice())) {
                error.put("UnitPrice", "Unit price field is empty or null");
            } else if (productDto.getUnitPrice() <= 0) {
                error.put("UnitPrice", "Unit price must be greater than 0");
            }

            // Validate stock
            if (ObjectUtils.isEmpty(productDto.getStock())) {
                error.put("Stock", "Stock field is empty or null");
            } else if (productDto.getStock() < 0) {
                error.put("Stock", "Stock cannot be negative");
            }

            // Validate discount percent (if provided)
            if (productDto.getDiscountPercent() != null) {
                if (productDto.getDiscountPercent() < 0 || productDto.getDiscountPercent() > 100) {
                    error.put("DiscountPercent", "Discount percent must be between 0 and 100");
                }
            }

            // Validate discounted price (if provided)
            if (productDto.getDiscountedPrice() != null && productDto.getDiscountedPrice() < 0) {
                error.put("DiscountedPrice", "Discounted price cannot be negative");
            }

            // Validate isActive
            if (ObjectUtils.isEmpty(productDto.getIsActive())) {
                error.put("IsActive", "IsActive field is empty or null");
            } else {
                if (productDto.getIsActive() != Boolean.TRUE.booleanValue() &&
                        productDto.getIsActive() != Boolean.FALSE.booleanValue()) {
                    error.put("IsActive", "Tried to add Invalid value in isActive field");
                }
            }

            // Validate category of products
            if (ObjectUtils.isEmpty(productDto.getCategory())) {
                error.put("Category", "Category field is empty or null");
            } else {
                // Check if category ID is present
                if (ObjectUtils.isEmpty(productDto.getCategory().getId())) {
                    error.put("CategoryId", "Category ID is required");
                }
            }
        }

        if (!error.isEmpty()) {
            throw new MyValidationException(error);
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------

    // Cart validation method
    public void validateAddToCartRequest(AddCartItems items) {
        Map<String, Object> error = new LinkedHashMap<>();

        // Validate product ID
        if (ObjectUtils.isEmpty(items.getProductId())) {
            error.put("ProductId", "Product ID cannot be null or empty");
        }

        // Validate quantity
        if (ObjectUtils.isEmpty(items.getQuantity())) {
            error.put("Quantity", "Quantity cannot be null or empty");
        } else if (items.getQuantity() <= 0) {
            error.put("Quantity", "Quantity must be greater than zero");
        }

        // Check if product exists and is active
        if (!ObjectUtils.isEmpty(items.getProductId())) {
            Optional<Product> productOpt = productRepo.findById(items.getProductId());
            if (productOpt.isEmpty()) {
                error.put("ProductId", "Product with the given ID does not exist");
            } else {
                Product product = productOpt.get();

                // Check if product is active
                if (!product.getIsActive()) {
                    error.put("ProductId", "Product is not active and cannot be added to cart");
                }

                // Check if product has sufficient stock
                if (items.getQuantity() > product.getStock()) {
                    error.put("Quantity", "Requested quantity exceeds available stock");
                }
            }
        }

        // Verify user is logged in
        try {
            SiteUser user = CommonUtil.getLoggedInUser();
            if (user == null) {
                error.put("User", "User must be logged in to add items to cart");
            }
        } catch (Exception e) {
            error.put("User", "Failed to retrieve logged-in user");
        }

        if (!error.isEmpty()) {
            throw new MyValidationException(error);
        }
    }

}
