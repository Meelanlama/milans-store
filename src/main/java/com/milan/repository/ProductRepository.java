package com.milan.repository;

import com.milan.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllActiveProducts(PageRequest pageRequest);


    // Get all inactive/soft-deleted products (admin only)
    Page<Product> findByIsActiveFalse(PageRequest pageRequest);


    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.productName)" +
            " LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchNotes(PageRequest pageRequest, String keyword);

}
