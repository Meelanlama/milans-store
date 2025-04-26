package com.milan.dto;

import com.milan.model.Category;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Integer id;

    private String productName;

    private String shortDescription;

    private String description;

    private Double unitPrice;

    private Integer stock;

    private String productImage;

    private Integer discountPercent;

    private Double discountedPrice;

    private Boolean isActive;

    private CategoryDto category;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    private Double averageRating;

    private Long totalReviews;


}
