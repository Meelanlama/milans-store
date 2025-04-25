package com.milan.repository;

import com.milan.model.Order;
import com.milan.model.Refund;
import com.milan.model.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {

    //check if a refund already exists for that order
    boolean existsByOrder(Order order);

    //get all refunds for a logged in user
    // Fetches paginated list of refunds where the order belongs to the given user
    // Filter refunds based on the provided user, i.e., `order.user.id = :userId`.
    Page<Refund> findByOrder_User(SiteUser loggedInUser, Pageable pageable);

}
