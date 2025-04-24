package com.milan.dto;

import com.milan.util.OrderStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderDto {

    private String orderIdentifier;

    private Double totalOrderAmount;

    private OrderStatus status;

    private String paymentMethod;

    private String shippingAddress;

    private String shippingZipCode;

    private String shippingProvince;

    private String shippingPhoneNumber;

    private LocalDateTime orderDate;

    private LocalDateTime estimatedDeliveryDate;

    private List<OrderItemDto> items;
}
