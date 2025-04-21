package com.milan.service.impl;

import com.milan.dto.CategoryDto;
import com.milan.dto.response.PageableResponse;
import com.milan.exception.ExistDataException;
import com.milan.exception.ResourceNotFoundException;
import com.milan.handler.PageMapper;
import com.milan.model.Category;
import com.milan.repository.CategoryRepository;
import com.milan.service.CategoryService;
import com.milan.util.CheckValidation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;

    private final ModelMapper mapper;

    private final CheckValidation checkValidation;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Override
    public Boolean createCategory(CategoryDto categoryDto) {

        //Validation check
        checkValidation.categoryValidation(categoryDto);

        //check if category_name already exists in db
        boolean exists = categoryRepo.existsCategoriesByCategoryName(categoryDto.getCategoryName());

        if(exists){
            throw new  ExistDataException("Category already exists");
        }

        logger.info("Creating new category: {}", categoryDto);

        //converting dto to entity
        Category category = mapper.map(categoryDto, Category.class);
        Category saveCategory = categoryRepo.save(category);

        logger.info("Category created successfully: {}", saveCategory);
        return true;
    }

    @Override
    public CategoryDto getCategoryById(Integer categoryId) {
        Category category = categoryRepo.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Invalid category id"));

        logger.info("Category found by id: {}", category);
        return mapper.map(category, CategoryDto.class);
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, Integer categoryId) {

        // Fetch category by ID or throw exception
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

        // update category object details to categoryDto
        category.setCategoryName(categoryDto.getCategoryName());
        category.setDescription(categoryDto.getDescription());

        // if categoryDto has categoryImage then update categoryImage in category object
        //otherwise leave the old categoryImage as it is
        if (categoryDto.getCategoryImage() != null) {
            category.setCategoryImage(categoryDto.getCategoryImage());
        }

        if(categoryDto.getIsActive() != null){
            category.setIsActive(categoryDto.getIsActive());
        }

        Category updatedCategory = categoryRepo.save(category);
        logger.info("Category updated successfully: {}", updatedCategory);
        return mapper.map(updatedCategory, CategoryDto.class);
    }

    @Override
    public Boolean deleteCategory(Integer categoryId) {
        Optional<Category> categoryById = categoryRepo.findById(categoryId);

        if(categoryById.isPresent()){
            Category category = categoryById.get();
            category.setIsActive(false);
            categoryRepo.save(category);
            logger.info("Soft deleted category with ID: {}", categoryId);
            return true;
        }
        return false;
    }

    @Override
    public PageableResponse<CategoryDto> getAllCategories(int pageNo, int pageSize, String sortBy, String sortDir) {

        //ternary operator for checking sortDir value
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, sort);

        //get categories that are active and not soft deleted
        Page<Category> categories = categoryRepo.findAllActiveCategories(pageRequest);

        //convert pageable category entity to dto class
        PageableResponse<CategoryDto> categoryResponse = PageMapper.getPageableResponse(categories,CategoryDto.class);

        return categoryResponse;

    }
}
