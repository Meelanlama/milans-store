package com.milan.service.impl;

import com.milan.dto.ReviewDto;
import com.milan.dto.ReviewRequestDto;
import com.milan.handler.PageableResponse;
import com.milan.exception.ResourceNotFoundException;
import com.milan.handler.PageMapper;
import com.milan.model.Product;
import com.milan.model.Review;
import com.milan.model.SiteUser;
import com.milan.repository.OrderItemRepository;
import com.milan.repository.OrderRepository;
import com.milan.repository.ProductRepository;
import com.milan.repository.ReviewRepository;
import com.milan.service.ReviewService;
import com.milan.util.CommonUtil;
import com.milan.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;

    private final ProductRepository productRepository;

    private final OrderRepository orderRepository;

    private final ModelMapper mapper;

    private final OrderItemRepository orderItemRepository;

    @Override
    public ReviewDto addReview(Integer productId, ReviewRequestDto reviewRequestDto) {

        //get current logged in user
        SiteUser user = CommonUtil.getLoggedInUser();

        // Fetch the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if the user has ordered the product and the order is DELIVERED
        // The OrderItem entity does not directly have user or status. But we can still access them through its order relationship.
        boolean hasDeliveredOrder = orderItemRepository.existsByOrder_UserAndProductAndOrder_Status(user, product, OrderStatus.DELIVERED);

        if (!hasDeliveredOrder) {
            throw new IllegalStateException("You can only review a product after it is delivered.");
        }

        //Check if user has already reviewed this product
        boolean alreadyReviewed = reviewRepository.existsByUserAndProduct(user, product);
        if (alreadyReviewed) {
            throw new IllegalStateException("You have already reviewed this product.");
        }

        //validate rating
        if(reviewRequestDto.getRating() < 1 || reviewRequestDto.getRating() > 5){
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        //Save review
        Review review = Review.builder()
                .rating(reviewRequestDto.getRating())
                .comment(reviewRequestDto.getComment())
                .product(product)
                .user(user)
                .build();

        Review savedReview = reviewRepository.save(review);

        logger.info("Review created for productId={} with review={}", productId, savedReview);

        // Map the Review entity to ReviewDto
        return mapper.map(savedReview, ReviewDto.class);
    }

    @Override
    public void updateReview(Integer reviewId, ReviewRequestDto reviewRequestDto) throws AccessDeniedException {

        //get current logged in user
        SiteUser user = CommonUtil.getLoggedInUser();

        // Find the existing review by ID or throw exception
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        // Only allow the user who created the review to update it
        if (!existingReview.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to update this review.");
        }

        // Update the review's fields
        existingReview.setRating(reviewRequestDto.getRating());
        existingReview.setComment(reviewRequestDto.getComment());
        existingReview.setUpdatedOn(LocalDateTime.now());

        // Save the updated review
        reviewRepository.save(existingReview);
    }

    @Override
    public void deleteReview(Integer reviewId) throws AccessDeniedException {

        //get current logged in user
        SiteUser user = CommonUtil.getLoggedInUser();

        // Find the existing review by ID or throw exception
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        // Only allow the user who created the review to update it
        if (!existingReview.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this review.");
        }
        // Delete the review
        reviewRepository.delete(existingReview);
    }

    // Calculate the average rating for a given product
    @Override
    public Double getAverageRating(Integer productId) {

        // Get all reviews for the product
        List<Review> reviews = reviewRepository.findByProductId(productId);

        // If there are no reviews, return 0.0 as the average
        if (reviews == null || reviews.isEmpty()) return 0.0;

        // Calculate and return the average rating
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    @Override
    public String getFormattedAverageRating(Integer productId) {

        Double avg = getAverageRating(productId);

        // Return the average rating as a formatted string (e.g., "4.2")
        return String.format("%.1f", avg);
    }

    @Override
    public Long getTotalReviews(Integer productId) {
        return reviewRepository.countByProductId(productId);
    }

    @Override
    public PageableResponse<ReviewDto> getReviewsByProduct(Integer productId, int pageNo, int pageSize,String sortBy,String sortDir) {

        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Fetch the product first as it's needed because findByProduct expects Product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid product id"));

        // Fetch reviews by product with paging
        Page<Review> reviewPage = reviewRepository.findByProduct(product, pageable);

        // forcefully initialize user
//        for (Review review : reviewPage) {
//            review.getUser().getFirstName();
//        }

        // manually map each Review → ReviewDto as we're also mapping full name in review details
        List<ReviewDto> reviewDtos = reviewPage.getContent().stream().map(review -> ReviewDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewerName(review.getUser().getFirstName().concat(" ").concat(review.getUser().getLastName()))
                .createdOn(review.getCreatedOn())
                .build()).toList();


        // Wrap the mapped review into a pageable response
        PageableResponse<ReviewDto> response = new PageableResponse<>();
        response.setContent(reviewDtos);
        response.setPageNo(reviewPage.getNumber());
        response.setPageSize(reviewPage.getSize());
        response.setTotalElements(reviewPage.getTotalElements());
        response.setTotalPages(reviewPage.getTotalPages());
        response.setIsFirst(reviewPage.isFirst());
        response.setIsLast(reviewPage.isLast());

        return response;
    }

    @Override
    public PageableResponse<ReviewDto> getReviewsOfUser(int pageNo, int pageSize, String sortBy, String sortDir) {

        // Get the logged-in user
        SiteUser user = CommonUtil.getLoggedInUser();

        //ternary operator for checking sortDir value
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());

        Pageable pageRequest = PageRequest.of(pageNo, pageSize, sort);

        Page<Review> reviews = reviewRepository.findByUser(user, pageRequest);


        // No need to check if empty, just return
       // frontend: If content.length > 0, show the list of reviews.

//        if(reviews.isEmpty()){
//            throw new ResourceNotFoundException("No reviews found");
//        }

        //Here we dont need to pass reviewer name like in the product one as it's our own review

        return PageMapper.getPageableResponse(reviews,ReviewDto.class);
    }

}


