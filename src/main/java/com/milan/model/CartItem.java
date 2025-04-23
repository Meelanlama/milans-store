package com.milan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem extends BaseDates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cartItemId;

    private Integer quantity;

    // Total price for this item = quantity × product.discountedPrice
    private Double subTotalPrice;

    // Many cart items can be for the same product (Many-to-One)
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Many cart items can belong to the same cart (Many-to-One)
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

}
