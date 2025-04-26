package com.milan.service;

import com.milan.dto.ReviewDto;
import com.milan.dto.ReviewRequestDto;
import com.milan.dto.response.PageableResponse;
import org.springframework.data.domain.Page;

import java.nio.file.AccessDeniedException;

public interface ReviewService {

    ReviewDto addReview(Integer productId, ReviewRequestDto reviewRequestDto);

    void updateReview(Integer reviewId, ReviewRequestDto reviewRequestDto) throws AccessDeniedException;

    void deleteReview(Integer reviewId) throws AccessDeniedException;

    Double getAverageRating(Integer productId);

     String getFormattedAverageRating(Integer productId);

    Long getTotalReviews(Integer productId);

    PageableResponse<ReviewDto> getReviewsByProduct(Integer productId, int pageNo, int pageSize,String sortBy,String sortDir);

    PageableResponse<ReviewDto> getReviewsOfUser(int pageNo, int pageSize, String sortBy, String sortDir);

}
