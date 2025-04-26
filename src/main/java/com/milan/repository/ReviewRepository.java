package com.milan.repository;

import com.milan.model.Product;
import com.milan.model.Review;
import com.milan.model.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // Check if a user has already reviewed a product
    boolean existsByUserAndProduct(SiteUser user, Product product);

    // Get all reviews for a specific product
    //helpful for getting reviews in paged results which is perfect for large data and frontend paging
    Page<Review> findByProduct(Product product, Pageable pageable);

    // Get a review by id and user (for edit/delete authorization)
    Optional<Review> findByIdAndUser(Integer reviewId, SiteUser user);

    // Finds all reviews by a specific user.
    Page<Review> findByUser(SiteUser user, Pageable pageable);

    // Finds all reviews for a specific product.
    // helpful for getting total review count
    List<Review> findByProductId(Integer productId);

    Long countByProductId(Integer productId);
}
