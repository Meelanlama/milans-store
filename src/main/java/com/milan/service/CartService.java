package com.milan.service;

import com.milan.dto.AddCartItems;
import com.milan.dto.CartDto;

import java.nio.file.AccessDeniedException;

public interface CartService {

    CartDto addItemsToCarts(AddCartItems items);

    void removeItemFromCart(int cartItemId) throws AccessDeniedException;

    void clearCart();

    CartDto getCartByUser();

}
