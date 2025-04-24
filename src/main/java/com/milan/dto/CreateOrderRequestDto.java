package com.milan.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreateOrderRequestDto {

    private Integer cartId;

    private String paymentMethod;

    private String shippingAddress;

    private String shippingZipCode;

    private String shippingProvince;

    private String shippingPhoneNumber;

}
