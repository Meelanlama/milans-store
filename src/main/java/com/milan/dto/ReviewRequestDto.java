package com.milan.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReviewRequestDto {

    @Min(1) @Max(5)
    private Integer rating;
    private String comment;

}
