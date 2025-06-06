package com.milan.controller;

import com.milan.dto.AddCartItems;
import com.milan.dto.CartDto;
import com.milan.service.CartService;
import com.milan.util.CommonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

//@SecurityRequirement(name = "Authorization")
@Tag(name = "CART MANAGEMENT", description = "Endpoints for managing user carts")
@RestController
@RequestMapping("${api.prefix}/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CartController.class);

    @Operation(summary = "Add items to cart", description = "Creates a new cart or adds items to the existing cart of the logged-in user.")
    //create cart and add items in that cart
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated() and hasRole('USER')")
    public ResponseEntity<?> addItemsToCart(@RequestBody AddCartItems items) {

        //  get logged in user from security context
        //  Already added logged in user in service as it'll handle it
        //SiteUser user = CommonUtil.getLoggedInUser();

        CartDto cartDto = cartService.addItemsToCarts(items);

        logger.info("Adding items to cart of loggedin user:", items);

        return CommonUtil.createBuildResponse(cartDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Remove item from cart", description = "Removes a specific item from the user's cart by item ID.")
    @DeleteMapping("/remove/{cartItemId}")
    @PreAuthorize("isAuthenticated() and hasRole('USER')")
    public ResponseEntity<?> removeItem(@PathVariable int cartItemId) throws AccessDeniedException {

        cartService.removeItemFromCart(cartItemId);

        logger.info("Removing item from user's cart", cartItemId);

        return CommonUtil.createBuildResponseMessage("Item removed successfully from cart", HttpStatus.OK);
    }

    @Operation(summary = "Clear cart", description = "Removes all items from the logged-in user's cart.")
    @PreAuthorize("isAuthenticated() and hasRole('USER')")
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {

        cartService.clearCart();

        logger.info("Clearing cart");

        return CommonUtil.createBuildResponseMessage("Cart cleared successfully", HttpStatus.OK);
    }

    @Operation(summary = "View my cart", description = "Fetches the cart items of the logged-in user.")
    @PreAuthorize("isAuthenticated() and hasRole('USER')")
    @GetMapping("/myCart")
    public ResponseEntity<?> getMyCart() {

        CartDto cartByUser = cartService.getCartByUser();

        logger.info("Getting logged in user's cart");

        return CommonUtil.createBuildResponse(cartByUser,HttpStatus.OK);

    }

}
