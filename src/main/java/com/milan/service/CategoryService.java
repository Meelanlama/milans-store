package com.milan.service;

import com.milan.dto.CategoryDto;
import com.milan.handler.PageableResponse;

public interface CategoryService {

    // Creates a new category and returns the created category DTO
    Boolean createCategory(CategoryDto categoryDto);

    CategoryDto getCategoryById(Integer categoryId);

    CategoryDto updateCategory(CategoryDto categoryDto, Integer categoryId);

    Boolean deleteCategory(Integer categoryId);

    PageableResponse<CategoryDto> getAllCategories(int pageNo, int pageSize, String sortBy, String sortDir);

}
