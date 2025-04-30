package com.milan.service;

import com.milan.dto.ProductDto;
import com.milan.handler.PageableResponse;

public interface ProductService {

    //create
    Boolean createProducts(ProductDto productDto);

    ProductDto getProductById(Integer productId);

    ProductDto updateProduct(ProductDto productDto, Integer productId);

    PageableResponse<ProductDto> getAllProducts(int pageNo, int pageSize, String sortBy, String sortDir);

    PageableResponse<ProductDto> getInactiveProducts(int pageNo, int pageSize, String sortBy, String sortDir);

    Boolean deleteProduct(Integer productId);

    PageableResponse<ProductDto> getProductsBySearching(String keyword,int pageNo, int pageSize, String sortBy, String sortDir);

    PageableResponse<ProductDto> getAllProductsByCategory(String categoryId, int pageNumber, int pageSize, String sortBy, String sortDir);

}
