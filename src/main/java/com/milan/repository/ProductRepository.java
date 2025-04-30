package com.milan.repository;

import com.milan.model.Category;
import com.milan.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllActiveProducts(Pageable pageRequest);


    // Get all inactive/soft-deleted products (admin only)
    Page<Product> findByIsActiveFalse(Pageable pageRequest);


    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.productName)" +
            " LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchNotes(Pageable pageRequest, String keyword);

    Page<Product> findByCategoryAndIsActiveTrue(Category category, Pageable pageRequest);

    List<Product> findByIsDeletedTrueAndDeletedOnBefore(LocalDateTime deleteProductsDay);

}
