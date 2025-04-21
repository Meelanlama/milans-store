package com.milan.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private Integer id;

    private String categoryName;

    private String categoryImage;

    private String description;

    private Boolean isActive;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

}
