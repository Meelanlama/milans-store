package com.milan.dto;

import com.milan.util.RefundStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequestDto {

    //private String orderIdentifier;  // To transfer the order ID instead of the full Order object
    private String reason;
    //private Double totalRefundAmount;
    //private LocalDateTime createdOn;  // From BaseDates

}
