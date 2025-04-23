package com.milan.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cart extends BaseDates{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;

    private Double totalCartPrice;

    // Only one cart is assigned to user
    // A user can have one active cart at a time (One-to-One)
    @OneToOne
    @JoinColumn(name="user_id")
    private SiteUser user;

    // One cart can have many cart items (One-to-Many)
    // 'mappedBy = "cart": the CartItem entity owns the relationship
    // Cascade = ALL: changes to Cart will affect its items
    // orphanRemoval = true: if item is removed from cart, it's deleted from DB
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

}
