package com.milan.model;

import com.milan.util.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order extends BaseDates{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderIdentifier;

    // Total price of the order
    private Double totalOrderAmount;

    // Status stored in enum
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String paymentMethod;

    //can be separated into an Address entity if needed/ i have used this approach for simplicity
    @Column(nullable = false)
    private String shippingAddress;

    @Column(length = 6, nullable = false)
    private String shippingZipCode;

    @Column(nullable = false)
    private String shippingProvince;

    @Column(nullable = false)
    private String shippingPhoneNumber ;

    private LocalDateTime orderDate;

    private LocalDateTime estimatedDeliveryDate;

    // Who placed the order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private SiteUser user;

    // Items in the order
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // 1:1 Relationship with Refund entity
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Refund refund;

}

