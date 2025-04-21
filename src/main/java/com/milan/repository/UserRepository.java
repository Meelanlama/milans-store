package com.milan.repository;

import com.milan.model.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SiteUser, Integer> {

    Optional<SiteUser> findByEmail(String email);

    boolean existsByEmail(String email);


}
