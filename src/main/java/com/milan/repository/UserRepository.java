package com.milan.repository;

import com.milan.model.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SiteUser, Integer> {

    Optional<SiteUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<SiteUser> findByRoles_Name(String roleName, Pageable pageable);


    @Query("SELECT u FROM SiteUser u " +
            "WHERE (:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER (CONCAT('%', :firstName, '%')))")
    Page<SiteUser> searchUsers(String firstName, Pageable pageable);

}
