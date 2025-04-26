package com.milan.controller;

import com.milan.dto.ReviewDto;
import com.milan.dto.ReviewRequestDto;
import com.milan.dto.response.PageableResponse;
import com.milan.model.Review;
import com.milan.model.SiteUser;
import com.milan.service.ReviewService;
import com.milan.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

import static com.milan.util.MyConstants.*;

@RestController
@RequestMapping(path = "/store/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    // Endpoint to add a review
    @PostMapping("create/{productId}")
    public ResponseEntity<?> createReview(@PathVariable Integer productId, @RequestBody ReviewRequestDto reviewRequestDto) {

        reviewService.addReview(productId, reviewRequestDto);

        return CommonUtil.createBuildResponseMessage("Review submitted successfully", HttpStatus.OK);
    }

    // Endpoint to update a review
    @PutMapping("edit/{reviewId}")
    @PreAuthorize(ROLE_USER)
    public ResponseEntity<?> updateReview(@PathVariable Integer reviewId,
                                               @RequestBody ReviewRequestDto reviewRequestDto) throws AccessDeniedException {

        reviewService.updateReview(reviewId, reviewRequestDto);

        return CommonUtil.createBuildResponseMessage("Review updated successfully", HttpStatus.OK);
    }

    // Endpoint to delete a review
    @DeleteMapping("/{reviewId}")
    @PreAuthorize(ROLE_USER)
    public ResponseEntity<?> deleteReview(@PathVariable Integer reviewId) throws AccessDeniedException {

        reviewService.deleteReview(reviewId);

        return CommonUtil.createBuildResponseMessage("Review deleted successfully", HttpStatus.OK);
    }

    //GET ALL REVIEWS IN PAGEABLE FORMAT FOR THAT SPECIFIC PRODUCT
    //Can access by all without login
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsForProduct(
            @PathVariable Integer productId,
            @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdOn") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        PageableResponse<ReviewDto> reviews = reviewService.getReviewsByProduct(productId, pageNo, pageSize,sortBy,sortDir);

        return CommonUtil.createBuildResponse(reviews, HttpStatus.OK);
    }


    // Endpoint to get the average rating as a number (e.g., 4.2)
    //Can access by all
    @GetMapping("/average-rating/{productId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable Integer productId) {
        Double average = reviewService.getAverageRating(productId);
        return ResponseEntity.ok(average);
    }

    //Endpoint to get formatted average rating (e.g., "4.2" as String)
    //makes the frontend cleaner and the UX more polished.
    @GetMapping("/average-rating-formatted/{productId}")
    public ResponseEntity<String> getFormattedAverageRating(@PathVariable Integer productId) {
        String formatted = reviewService.getFormattedAverageRating(productId);
        return ResponseEntity.ok(formatted);
    }

    //Get all the reviews reviewed by the logged in user
    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(@RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO) int pageNo,
                                          @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
                                          @RequestParam(value = "sortBy", defaultValue = "createdOn") String sortBy,
                                          @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        PageableResponse<ReviewDto> reviewDto = reviewService.getReviewsOfUser(pageNo,pageSize,sortBy,sortDir);

        return CommonUtil.createBuildResponse(reviewDto, HttpStatus.OK);

    }

}
