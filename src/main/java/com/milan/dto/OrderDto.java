package com.milan.dto;

import com.milan.enums.OrderStatus;
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

    //Added this to show details of user in order.
    //helps in showing order history of user while exporting in excel.
    private UserDto user;

}
