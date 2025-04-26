package com.milan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.milan.dto.ProductDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "reviews")
@Builder
public class Review extends BaseDates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Rating given by the user (e.g., 1 to 5 stars)
    @Column(nullable = false)
    @Min(1) @Max(5)
    private Integer rating;

    // Comment provided by the user
    private String comment;

    //Many reviews can be written for the same product, but each review is linked to only one product.
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    // A review is made by a specific user and references user id
    // Many reviews can be written by a single user, but each review is linked to only one user.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private SiteUser user;

}
