package com.milan.repository;

import com.milan.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsCategoriesByCategoryName(String categoryName);

    @Query("SELECT c FROM Category c WHERE c.isActive = true")
    Page<Category> findAllActiveCategories(PageRequest pageRequest);

}
