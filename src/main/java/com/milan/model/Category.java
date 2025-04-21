package com.milan.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Category extends BaseDates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 60, nullable = false)
    private String categoryName;

    private String categoryImage; // Stores URL/path to the image

    private String description;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive;

}

