package com.milan.service.impl;

import com.milan.dto.AddCartItems;
import com.milan.dto.CartDto;
import com.milan.exception.ResourceNotFoundException;
import com.milan.model.Cart;
import com.milan.model.CartItem;
import com.milan.model.Product;
import com.milan.model.SiteUser;
import com.milan.repository.CartItemRepository;
import com.milan.repository.CartRepository;
import com.milan.repository.ProductRepository;
import com.milan.service.CartService;
import com.milan.util.CheckValidation;
import com.milan.util.CommonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;

    private final CartItemRepository cartItemRepo;

    private final ProductRepository productRepo;

    private final ModelMapper mapper;

    private final CheckValidation checkValidation;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CartServiceImpl.class);

    @Override
    public CartDto addItemsToCarts(AddCartItems items) {

        //Validation check
        checkValidation.validateAddToCartRequest(items);

        int productId = items.getProductId();
        int quantity = items.getQuantity();

        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity in cart must be greater than zero");
        }

        // Get product
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid product id"));

        // Get logged-in user
        SiteUser user = CommonUtil.getLoggedInUser();

        // Get or create cart
        Cart cart = cartRepo.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return newCart;
        });


        // Check if item already in cart
        boolean itemUpdated = false;
        for (CartItem item : cart.getCartItems()) {
            if (item.getProduct().getId() == productId) {
                item.setQuantity(quantity);
                item.setSubTotalPrice(quantity * product.getDiscountedPrice());
                itemUpdated = true;
                break;
            }
        }

        // If item is not in cart, add new item
        if (!itemUpdated) {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .subTotalPrice(quantity * product.getDiscountedPrice())
                    .build();
            cart.getCartItems().add(newItem);
        }

        // Calculate total price for the cart
        double totalCartPrice = cart.getCartItems().stream()
                .mapToDouble(CartItem::getSubTotalPrice)
                .sum();
        cart.setTotalCartPrice(totalCartPrice);  // Set the total price in the cart

        // Save cart
        Cart savedCart = cartRepo.save(cart);

        // Return DTO
        return mapper.map(savedCart, CartDto.class);

    }

    @Override
    @Transactional     // Ensures DB operations are atomic (all succeed or none)
    public void removeItemFromCart(int cartItemId) throws AccessDeniedException {

        // Get the logged-in user
        SiteUser user = CommonUtil.getLoggedInUser();

        // Find the CartItem by its ID
        CartItem cartItem = cartItemRepo.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        //Check if item belongs to the user's cart and then only delete that item
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can't remove items from someone else's cart");
        }

        // Remove single item from cart_items and from cart also by its id
        Cart cart = cartItem.getCart();
        cart.getCartItems().remove(cartItem);

        // Delete the CartItem from the database
        cartItemRepo.delete(cartItem);

        // Recalculate totalCartPrice again after deleting cart item
        double updatedTotal = cart.getCartItems().stream()
                .mapToDouble(CartItem::getSubTotalPrice)
                .sum();

        cart.setTotalCartPrice(updatedTotal);

        //save after deleting cart item
        cartRepo.save(cart);
    }


    @Override
    public void clearCart() {
        SiteUser user = CommonUtil.getLoggedInUser();

        Cart cart = cartRepo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for this user"));

        // Remove all items in that cart
        cart.getCartItems().clear();
        cartRepo.save(cart);

        // If no items remain, delete the cart and it's details related to the items
        if (cart.getCartItems().isEmpty()) {
            cartRepo.delete(cart);
        }

    }


    @Override
    public CartDto getCartByUser() {
        SiteUser user = CommonUtil.getLoggedInUser();

        Cart cart = cartRepo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for this user"));

        return mapper.map(cart, CartDto.class);
    }

}
