package com.filadelfia.store.filadelfiastore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.enums.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> getUserByEmail(String email);
    List<User> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    boolean existsByEmail(String email);
    List<User> findByActiveTrue();
    List<User> findByRole(UserRole role);
    boolean existsByRole(UserRole role);
}