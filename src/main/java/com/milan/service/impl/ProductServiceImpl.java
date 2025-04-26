package com.milan.service.impl;

import com.milan.dto.CategoryDto;
import com.milan.dto.ProductDto;
import com.milan.dto.response.PageableResponse;
import com.milan.exception.ResourceNotFoundException;
import com.milan.handler.PageMapper;
import com.milan.model.Category;
import com.milan.model.Product;
import com.milan.repository.CategoryRepository;
import com.milan.repository.ProductRepository;
import com.milan.service.ImageService;
import com.milan.service.ProductService;
import com.milan.util.CheckValidation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ImageService imageService;

    private final ModelMapper mapper;

    private final ProductRepository productRepo;

    private final CategoryRepository categoryRepo;

    private final CheckValidation checkValidation;

    private final ReviewServiceImpl reviewService;


    @Override
    public Boolean createProducts(ProductDto productDto) {

        //Validation check
        checkValidation.productValidation(productDto);

        //convert productDto to product entity
        Product product = mapper.map(productDto, Product.class);

        // Calculate discounted price and set automatically
        if (productDto.getDiscountPercent() > 0
                && productDto.getUnitPrice() > 0
                && productDto.getDiscountPercent() <= 100) {

            double discount = productDto.getUnitPrice() * productDto.getDiscountPercent() / 100;
            product.setDiscountedPrice(productDto.getUnitPrice() - discount);

        } else {
            // if discountPercent is not between 0 and 100 then set unitPrice as discountedPrice
            product.setDiscountedPrice(productDto.getUnitPrice());
        }

        //saving product to db
        Product saveProduct = productRepo.save(product);

        logger.info("Product creating: {}", saveProduct);

        //returns true if it's not empty otherwise false
        return !ObjectUtils.isEmpty(saveProduct);
    }

    //helper method to add total reviews and avg rating in product dto
    private void addRatingsAndReviewInDto(ProductDto productDto) {
        Double avgRating = reviewService.getAverageRating(productDto.getId());
        Long totalReviews = reviewService.getTotalReviews(productDto.getId());
        productDto.setAverageRating(avgRating);
        productDto.setTotalReviews(totalReviews);
    }

    @Override
    public ProductDto getProductById(Integer productId) {
        Product foundProduct = productRepo.findById(productId).orElseThrow(() -> new IllegalArgumentException("Invalid product id"));
        logger.info("Product found by id: {}", foundProduct);

        //convert product entity to productDto
        ProductDto productDto =  mapper.map(foundProduct, ProductDto.class);

        //set total reviews and ratings in product dto itself by helper method
        addRatingsAndReviewInDto(productDto);

        return productDto;
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto, Integer productId) {

        //get product by id
        Product foundProduct = productRepo.findById(productId).orElseThrow(() -> new IllegalArgumentException("Invalid product id"));

        //update product entity with productDto values with helper method updateFromDto() in product entity class
        Product updatedProduct = foundProduct.updateFromDto(productDto);

        // Handle category separately, if category id is not passed then don't update it and old one won't change
        if(productDto.getCategory() != null) {
            Category category = categoryRepo.findById(productDto.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            updatedProduct.setCategory(category);
        }

        productRepo.save(updatedProduct);
        logger.info("Product updated successfully: {}", updatedProduct);
        return mapper.map(updatedProduct, ProductDto.class);
    }

    @Override
    public PageableResponse<ProductDto> getAllProducts(int pageNo, int pageSize, String sortBy, String sortDir) {

        //ternary operator for checking sortDir value
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        Pageable pageRequest = PageRequest.of(pageNo, pageSize, sort);

        //get products that are active and not soft deleted
        Page<Product> allProducts = productRepo.findAllActiveProducts(pageRequest);

//        if(allProducts.isEmpty()){
//            throw new ResourceNotFoundException("No products found");
//        }

        //convert pageable product entity to dto class
        PageableResponse<ProductDto> productDtoPageableResponse = PageMapper.getPageableResponse(allProducts,ProductDto.class);

        // Set total reviews and ratings in product dto for each product
        //The frontend will now receive the full ProductDto with the additional fields for averageRating and totalReviews.
        productDtoPageableResponse.getContent().forEach(this::addRatingsAndReviewInDto);

        logger.info("Found all products {}", productDtoPageableResponse.getTotalElements());

        return productDtoPageableResponse;
    }

    @Override
    public PageableResponse<ProductDto> getInactiveProducts(int pageNo, int pageSize, String sortBy, String sortDir) {

        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        Pageable pageRequest = PageRequest.of(pageNo, pageSize, sort);

        Page<Product> inactiveProducts = productRepo.findByIsActiveFalse(pageRequest);

//        if(inactiveProducts.isEmpty()){
//            throw new ResourceNotFoundException("No products found");
//        }

        PageableResponse<ProductDto> productDtoPageableResponse = PageMapper.getPageableResponse(inactiveProducts,ProductDto.class);

        logger.info("Found all inactive products {}", productDtoPageableResponse.getTotalElements());

        return productDtoPageableResponse;
    }

    @Override
    public Boolean deleteProduct(Integer productId) {

        Optional<Product> byId = productRepo.findById(productId);

        if(byId.isPresent()){
            Product product = byId.get();
            product.setIsActive(false);
            productRepo.save(product);
            logger.info("Soft deleted product with ID: {}", productId);
            return true;
        }
        return false;
    }

    @Override
    public PageableResponse<ProductDto> getProductsBySearching(String keyword,int pageNo, int pageSize, String sortBy, String sortDir) {

        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        Pageable pageRequest = PageRequest.of(pageNo, pageSize, sort);

        //get products that are active only and match the keyword in product names or description
        Page<Product> searchedProducts = productRepo.searchNotes(pageRequest,keyword);

        if(searchedProducts.isEmpty()){
            throw new ResourceNotFoundException("No products related to the keyword");
        }

        //convert pageable product entity to dto class
        PageableResponse<ProductDto> productDtoPageableResponse = PageMapper.getPageableResponse(searchedProducts,ProductDto.class);

        productDtoPageableResponse.getContent().forEach(this::addRatingsAndReviewInDto);

        logger.info("Found searched products {}", productDtoPageableResponse.getTotalElements());

        return productDtoPageableResponse;
    }

    @Override
    public PageableResponse<ProductDto> getAllProductsByCategory(String categoryId, int pageNumber, int pageSize, String sortBy, String sortDir) {

        //find category by id first
        Category category = categoryRepo.findById(Integer.parseInt(categoryId)).orElseThrow(() -> new ResourceNotFoundException("Invalid category id"));

        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        Pageable pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productsByCategory = productRepo.findByCategoryAndIsActiveTrue(category, pageRequest);

//        if(productsByCategory.isEmpty()){
//            throw new ResourceNotFoundException("No products found in this category");
//        }

        PageableResponse<ProductDto> productDtoPageableResponse = PageMapper.getPageableResponse(productsByCategory,ProductDto.class);

        productDtoPageableResponse.getContent().forEach(this::addRatingsAndReviewInDto);

        logger.info("Found all products in this category {}", productDtoPageableResponse.getTotalElements());

        return productDtoPageableResponse;

    }
}
