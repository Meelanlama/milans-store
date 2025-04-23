package com.milan.dto;

import com.milan.model.CartItem;
import com.milan.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    private int cartItemId;

    private Integer quantity;

    private Double subTotalPrice;

    private ProductDto product;

}
