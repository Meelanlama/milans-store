package com.milan.repository;

import com.milan.model.Order;
import com.milan.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findByUserId(Integer id, Pageable pageable);

    Optional<Order> findByOrderIdentifier(String orderIdentifier);


    //FILTER ORDERS BY STATUS, START DATE, END DATE IN SQL
//    SELECT * FROM orders
//    WHERE status = 'SHIPPED'
//    AND order_date >= '2025-04-01 00:00:00'
//    AND order_date <= '2025-04-30 23:59:59';

    @Query("SELECT o FROM Order o WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<Order> findAllWithFilters(@Param("status") OrderStatus orderStatus,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);


}
