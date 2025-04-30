package com.milan.controller;

import com.milan.dto.CategoryDto;
import com.milan.dto.ProductDto;
import com.milan.dto.response.ImageResponse;
import com.milan.handler.PageableResponse;
import com.milan.service.CategoryService;
import com.milan.service.ImageService;
import com.milan.service.ProductService;
import com.milan.util.CommonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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

import static com.milan.util.MyConstants.*;

//@SecurityRequirement(name = "Authorization")
@Tag(name = "CATEGORY MANAGEMENT", description = "APIs for Category CRUD operations")
@RequestMapping(path = "${api.prefix}/category")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    private final ImageService imageService;

    private final ProductService productService;

    @Value("${image.category}")
    private String categoryImagePath;

    @Operation(summary = "Create new category", description = "This API creates a new category and returns success/failure message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to create category")
    })
    @PostMapping("/create")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> createCategory(@RequestBody CategoryDto categoryDto) {

        //returns true if category is saved successfully, false otherwise
        Boolean saved = categoryService.createCategory(categoryDto);

        if (saved) {
            return CommonUtil.createBuildResponseMessage("Category saved", HttpStatus.CREATED);
        }
        return CommonUtil.createErrorResponseMessage("Category not saved", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Operation(summary = "Upload category image", description = "This API uploads an image for a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to upload image")
    })
    @PostMapping("/upload-image/{categoryId}")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> uploadCategoryImage(@PathVariable("categoryId") Integer categoryId, @RequestParam("image") MultipartFile image) throws IOException {

        String imageName = imageService.uploadImage(image,categoryImagePath);

        // get category by id and set the image name for the category
        CategoryDto categoryDtoById = categoryService.getCategoryById(categoryId);
        categoryDtoById.setCategoryImage(imageName);

        // Save category with updated image name
        categoryService.updateCategory(categoryDtoById,categoryId);

       ImageResponse response = ImageResponse.builder()
                .imageName(imageName)
                .message("Image updated")
                .success(true)
                .status(HttpStatus.CREATED)
                .build();

       return CommonUtil.createBuildResponse(response,HttpStatus.CREATED);
    }

    @Operation(summary = "Display category image", description = "This API retrieves and displays the image for a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image displayed successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @GetMapping("/image/{categoryId}")
    public void displayCategoryImage(@PathVariable Integer categoryId, HttpServletResponse response) throws IOException {

        // 1. Get category from DB
        CategoryDto categoryDto = categoryService.getCategoryById(categoryId);

        // 2. Get image name stored in DBs
        String imageName = categoryDto.getCategoryImage();

        // 3. Load from local folder
        InputStream resource = imageService.getResource(categoryImagePath, imageName);

        // 4. Set content type and stream the data
        response.setContentType(MediaType.IMAGE_JPEG_VALUE); // or detect from file type
        StreamUtils.copy(resource, response.getOutputStream());
    }

    @Operation(summary = "Get category by ID", description = "This API retrieves a specific category by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer categoryId) {

        CategoryDto categoryById = categoryService.getCategoryById(categoryId);

        return CommonUtil.createBuildResponse(categoryById,HttpStatus.OK);
    }

    @Operation(summary = "Update category", description = "This API updates an existing category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/update/{categoryId}")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> updateCategory(@RequestBody CategoryDto categoryDto, @PathVariable("categoryId") Integer categoryId) {

        CategoryDto updatedCategory = this.categoryService.updateCategory(categoryDto, categoryId);

        return CommonUtil.createBuildResponse(updatedCategory, HttpStatus.OK);

    }

    @Operation(summary = "Delete category", description = "This API deletes a category by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to delete category")
    })
    @DeleteMapping("/delete/{categoryId}")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> deleteCategory(@PathVariable("categoryId") Integer categoryId) {
        Boolean isDeleted = categoryService.deleteCategory(categoryId);

        if (!isDeleted) {
            return CommonUtil.createErrorResponseMessage("Category not deleted. The category might not exist.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return CommonUtil.createBuildResponseMessage("Category deleted successfully", HttpStatus.OK);

    }

    @Operation(summary = "Get all categories", description = "This API retrieves all categories with pagination and sorting options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping("/getAllCategories")
    public ResponseEntity<?> getAllCategories(
            @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "categoryName") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {


        //get all categories in pageable formats
        PageableResponse<CategoryDto> categoryResponse = this.categoryService.getAllCategories(pageNo, pageSize, sortBy, sortDir);

        return CommonUtil.createBuildResponse(categoryResponse, HttpStatus.OK);
    }

    @Operation(summary = "Get products by category", description = "This API retrieves all products belonging to a specific category with pagination and sorting options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    //this method is useful: when you want to get all products of that specific category only in pageable format in frontend
    @GetMapping("/{categoryId}/productsByCategory")
    public ResponseEntity<?> getProductsByCategoryId(@PathVariable String categoryId,
                                                     @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO, required = false) int pageNo,
                                                     @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                     @RequestParam(value = "sortBy", defaultValue = "productName", required = false) String sortBy,
                                                     @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir) {

        PageableResponse<ProductDto> response = productService.getAllProductsByCategory(categoryId, pageNo, pageSize, sortBy, sortDir);

        return CommonUtil.createBuildResponse(response, HttpStatus.OK);
    }

}
