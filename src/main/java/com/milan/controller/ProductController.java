package com.milan.controller;

import com.milan.dto.ProductDto;
import com.milan.dto.response.ImageResponse;
import com.milan.handler.PageableResponse;
import com.milan.service.ImageService;
import com.milan.service.ProductService;
import com.milan.util.CommonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

//Indicates that this controller's endpoints require an Authorization header with a Bearer token (JWT) in Swagger UI
//@SecurityRequirement(name = "Authorization")
@Tag(name = "PRODUCT MANAGEMENT", description = "APIs for managing products")
@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProductController.class);

    @Value("${image.product}")
    private String productImagePath;

    private final ProductService productService;

    private final ImageService imageService;

    @Operation(summary = "Create a new product", description = "Create a new product in the system (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "500", description = "Product creation failed")
    })
    //create products
    @PostMapping("/create")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> createProduct(@RequestBody ProductDto productDto){

        Boolean created = productService.createProducts(productDto);

        if (created) {
            logger.info("Product created successfully");
            return CommonUtil.createBuildResponseMessage("Product created successfully", HttpStatus.CREATED);
        }else {
            return CommonUtil.createErrorResponseMessage("Product creation failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Upload product image", description = "Upload image for a specific product (Admin only)")
    @ApiResponse(responseCode = "201", description = "Image uploaded successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ImageResponse.class)))
    @PostMapping("/upload-image/{productId}")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> uploadProductImage(@PathVariable("productId") Integer productId, @RequestParam("image") MultipartFile image) throws IOException {

        String imageName = imageService.uploadImage(image,productImagePath);

        // get category by id and set the image name for the category
        ProductDto productDtoById = productService.getProductById(productId);
        productDtoById.setProductImage(imageName);

        // Save category with updated image name
        productService.updateProduct(productDtoById,productId);

        ImageResponse response = ImageResponse.builder()
                .imageName(imageName)
                .message("Image updated")
                .success(true)
                .status(HttpStatus.CREATED)
                .build();

        return CommonUtil.createBuildResponse(response,HttpStatus.CREATED);
    }

    @Operation(summary = "Get product image", description = "Display image for a specific product")
    @ApiResponse(responseCode = "200", description = "Image retrieved successfully", content = @Content(mediaType = "image/jpeg"))
    @GetMapping("/image/{productId}")
    public void displayProductImage(@PathVariable Integer productId, HttpServletResponse response) throws IOException {

        // 1. Get category from DB
        ProductDto productDto = productService.getProductById(productId);

        // 2. Get image name stored in DBs
        String imageName = productDto.getProductImage();

        // 3. Load from local folder
        InputStream resource = imageService.getResource(productImagePath, imageName);

        // 4. Set content type and stream the data
        response.setContentType(MediaType.IMAGE_JPEG_VALUE); // or detect from file type
        StreamUtils.copy(resource, response.getOutputStream());
    }


    @Operation(summary = "Get product by ID", description = "Retrieve product details by product ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    // get only single product
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Integer productId) {
        ProductDto responseProductDto = this.productService.getProductById(productId);

        return CommonUtil.createBuildResponse(responseProductDto,HttpStatus.OK);
    }

    @Operation(summary = "Get all active products (paginated)", description = "Retrieve paginated list of active products")
    //get all the active products in pageable formats
    @GetMapping("/getActiveProducts")
    public ResponseEntity<?> getActiveProducts(
            @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "productName") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {


        //get all products in pageable formats
        PageableResponse<ProductDto> allProducts = productService.getAllProducts(pageNo, pageSize, sortBy, sortDir);

        //display all products response in pageable format
        return CommonUtil.createBuildResponse(allProducts, HttpStatus.OK);
    }

    @Operation(summary = "Get all inactive products (paginated)", description = "Retrieve paginated list of inactive products (Admin only)")
    @GetMapping("/getInactiveProducts")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> getInActiveProducts(
            @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "productName") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {


        PageableResponse<ProductDto> allInactiveProducts = productService.getInactiveProducts(pageNo, pageSize, sortBy, sortDir);

        return CommonUtil.createBuildResponse(allInactiveProducts, HttpStatus.OK);
    }

    @Operation(summary = "Update product details", description = "Update existing product information (Admin only)")
    //update product
    @PutMapping("/update/{productId}")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> updateProduct(@RequestBody ProductDto productDto, @PathVariable("productId") Integer productId) {

        //call service to update products
        ProductDto updatedProduct = this.productService.updateProduct(productDto, productId);
        return CommonUtil.createBuildResponse(updatedProduct, HttpStatus.OK);
    }

    @Operation(summary = "Delete a product", description = "Delete a product from the system (Admin only)")
    //delete product
    @DeleteMapping("/delete/{productId}")
    @PreAuthorize(ROLE_ADMIN)
    public ResponseEntity<?> deleteProduct(@PathVariable("productId") Integer productId) {

        Boolean isDeleted = productService.deleteProduct(productId);

        if (!isDeleted) {
            return CommonUtil.createErrorResponseMessage("Product not deleted. The product might not exist.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return CommonUtil.createBuildResponseMessage("Product deleted successfully", HttpStatus.OK);
    }

    @Operation(summary = "Search products by keyword", description = "Search products with pagination support")
    //search products
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam(value = "keyword", defaultValue = "") String keyword,
                                           @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
                                           @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
                                           @RequestParam(value = "sortBy", defaultValue = "productName") String sortBy,
                                           @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {


        PageableResponse<ProductDto> searchedProducts = productService.getProductsBySearching(keyword,pageNo, pageSize, sortBy, sortDir);

        return CommonUtil.createBuildResponse(searchedProducts, HttpStatus.OK);

    }


}
