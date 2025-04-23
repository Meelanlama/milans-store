package com.milan.repository;

import com.milan.model.Cart;
import com.milan.model.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Cart findByUserId(Integer userId);

    Optional<Cart> findByUser(SiteUser user);

}
