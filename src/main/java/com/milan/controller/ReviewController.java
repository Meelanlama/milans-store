package com.milan.controller;

import com.milan.dto.ReviewDto;
import com.milan.dto.ReviewRequestDto;
import com.milan.handler.PageableResponse;
import com.milan.service.ReviewService;
import com.milan.util.CommonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

import static com.milan.util.MyConstants.*;

//@SecurityRequirement(name = "Authorization")
@Tag(name = "REVIEW MANAGEMENT", description = "APIs for managing product reviews and ratings")
@RestController
@RequestMapping(path = "${api.prefix}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    @Operation(summary = "Submit a review for the delivered products only", description = "Add a new review for a specific delivered product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review submitted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // Endpoint to add a review
    @PostMapping("create/{productId}")
    public ResponseEntity<?> createReview(@PathVariable Integer productId, @RequestBody ReviewRequestDto reviewRequestDto) {

        reviewService.addReview(productId, reviewRequestDto);

        return CommonUtil.createBuildResponseMessage("Review submitted successfully", HttpStatus.OK);
    }

    @Operation(summary = "Update a review", description = "Update an existing review (User only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // Endpoint to update a review
    @PutMapping("edit/{reviewId}")
    @PreAuthorize(ROLE_USER)
    public ResponseEntity<?> updateReview(@PathVariable Integer reviewId,
                                               @RequestBody ReviewRequestDto reviewRequestDto) throws AccessDeniedException {

        reviewService.updateReview(reviewId, reviewRequestDto);

        return CommonUtil.createBuildResponseMessage("Review updated successfully", HttpStatus.OK);
    }

    @Operation(summary = "Delete a review", description = "Delete a review (User only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // Endpoint to delete a review
    @DeleteMapping("/{reviewId}")
    @PreAuthorize(ROLE_USER)
    public ResponseEntity<?> deleteReview(@PathVariable Integer reviewId) throws AccessDeniedException {

        reviewService.deleteReview(reviewId);

        return CommonUtil.createBuildResponseMessage("Review deleted successfully", HttpStatus.OK);
    }

    @Operation(summary = "Get product reviews", description = "Retrieve paginated reviews for a product (Public)")
    @ApiResponse(responseCode = "200", description = "List of reviews retrieved",
            content = @Content(schema = @Schema(implementation = PageableResponse.class)))
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


    @Operation(summary = "Get average rating", description = "Retrieve the numeric average rating of a product (Public)")
    @ApiResponse(responseCode = "200", description = "Average rating value",
            content = @Content(schema = @Schema(type = "number", format = "double")))
    // Endpoint to get the average rating as a number (e.g., 4.2)
    //Can access by all
    @GetMapping("/average-rating/{productId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable Integer productId) {
        Double average = reviewService.getAverageRating(productId);
        return ResponseEntity.ok(average);
    }

    @Operation(summary = "Get formatted average rating", description = "Retrieve the average rating as a formatted string (Public)")
    @ApiResponse(responseCode = "200", description = "Formatted average rating",
            content = @Content(schema = @Schema(type = "string", example = "4.2")))
    //Endpoint to get formatted average rating (e.g., "4.2" as String)
    //makes the frontend cleaner and the UX more polished.
    @GetMapping("/average-rating-formatted/{productId}")
    public ResponseEntity<String> getFormattedAverageRating(@PathVariable Integer productId) {
        String formatted = reviewService.getFormattedAverageRating(productId);
        return ResponseEntity.ok(formatted);
    }

    @Operation(summary = "Get user's reviews", description = "Retrieve paginated list of current user's reviews")
    @ApiResponse(responseCode = "200", description = "List of user reviews",
            content = @Content(schema = @Schema(implementation = PageableResponse.class)))
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
