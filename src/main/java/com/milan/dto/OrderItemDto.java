package com.milan.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItemDto {

    private Integer orderItemId;

    private int quantity;

    //show total price of item according to quantity
    private Double priceAtPurchase;

    private Integer productId;
}

