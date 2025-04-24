package com.milan.util;

public enum OrderStatus {
    IN_PROGRESS(1, "Order In Progress"),
    ORDER_RECEIVED(2, "Order Received"),
    ORDER_SHIPPED(3, "Order Shipped"),
    OUT_FOR_DELIVERY(4, "Trying to deliver your order today"),
    DELIVERED(5, "Delivery Successful"),
    CANCELLED(6, "Order Cancelled");

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
