package com.milan.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundSellerDto {

   // private RefundStatus status;  // Using the RefundStatus enum directly
    private String sellerComment;
    //private LocalDateTime resolvedDate;
    //private LocalDateTime updatedOn;  // From BaseDates

}
