package com.milan.dto;

import com.milan.model.Product;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReviewDto {

    private Integer id;
    private Integer rating;
    private String comment;

    // map from SiteUser.getName()
    //show who wrote the review, we get the reviewer name by custom mapping in Model mapper
    private String reviewerName;

    private LocalDateTime createdOn;

    //REMOVING: already know the product details as we'll fetch another api earlier when showing product details

    // This is a FULL entity here, not just a simple ID or name
    // This directly references the entity, causing circular dependencies.
    //When you fetch a Product, you maybe fetch its reviews, and each Review has a full Product inside it again and the cycle repeats forever.
    //Serialization (JSON) tries to walk through this infinitely → nesting exceeds 1000 → crash.
    //private Product product;

    // only ID, not full Product entity
    //private Integer productId;

}
