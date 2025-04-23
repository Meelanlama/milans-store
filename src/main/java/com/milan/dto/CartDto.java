package com.milan.dto;

import com.milan.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private Integer cartId;

    private UserDto user;

    private List<CartItemDto> items;

    private Double totalCartPrice;

}
