package com.milan.repository;

import com.milan.model.OrderItem;
import com.milan.model.Product;
import com.milan.model.SiteUser;
import com.milan.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    //check if an OrderItem exists for a user, a product, and with a specific order status
    //OrderItem has reference to Order and Order has reference to User and OrderStatus
    boolean existsByOrder_UserAndProductAndOrder_Status(SiteUser user, Product product, OrderStatus orderStatus);
}
