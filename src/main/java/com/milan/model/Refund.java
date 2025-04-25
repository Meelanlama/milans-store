package com.milan.model;

import com.milan.util.RefundStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Refund extends BaseDates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false) // Foreign key column in the Refund table
    private Order order;

    //"Damaged", "Not satisfied"
    private String reason;

    // Total amount to be refunded (the amount to be refunded to the user)
    //private Double totalRefundAmount;

    // Store the enum as a string in the database
    // Initially PENDING
    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;

    private String sellerComment;

    // The date when the refund is resolved/approved
    private LocalDateTime resolvedDate;

}
