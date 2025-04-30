package com.milan.enums;

public enum OrderStatus {
    IN_PROGRESS(1, "Order In Progress"),
    RECEIVED(2, "Order Received"),
    SHIPPED(3, "Order Shipped"),
    OUT_FOR_DELIVERY(4, "Trying to deliver your order today"),
    DELIVERED(5, "Delivery Successful"),
    CANCELLED(6, "Order Cancelled"),
    REFUNDED(7, "Order Refund Successful");

    private final Integer id;
    private final String label;

    OrderStatus(Integer id, String label) {
        this.id = id;
        this.label = label;
    }

    public Integer getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}
