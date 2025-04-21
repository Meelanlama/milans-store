package com.milan.controller;

import com.milan.dto.CategoryDto;
import com.milan.dto.response.ImageResponse;
import com.milan.dto.response.PageableResponse;
import com.milan.service.CategoryService;
import com.milan.service.ImageService;
import com.milan.util.CommonUtil;
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

@RequestMapping(path = "/store/v1/category")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    private final ImageService imageService;

    @Value("${image.category}")
    private String categoryImagePath;

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

    @GetMapping("/image/{categoryId}")
    public void serveCategoryImage(@PathVariable Integer categoryId, HttpServletResponse response) throws IOException {

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

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer categoryId) {
        CategoryDto categoryById = categoryService.getCategoryById(categoryId);
        return CommonUtil.createBuildResponse(categoryById,HttpStatus.OK);
    }

    @PutMapping("/update/{categoryId}")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> updateCategory(@RequestBody CategoryDto categoryDto,
                                                      @PathVariable("categoryId") Integer categoryId) {
        CategoryDto updatedCategory = this.categoryService.updateCategory(categoryDto, categoryId);
        return CommonUtil.createBuildResponse(updatedCategory, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{categoryId}")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> deleteCategory(@PathVariable("categoryId") Integer categoryId) {
        Boolean isDeleted = categoryService.deleteCategory(categoryId);

        if (!isDeleted) {
            return CommonUtil.createErrorResponseMessage("Category not deleted. The category might not exist.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return CommonUtil.createBuildResponseMessage("Category deleted successfully", HttpStatus.OK);
    }

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


}
