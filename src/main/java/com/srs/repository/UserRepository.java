package com.srs.repository;

import com.srs.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);
    Optional<User> findByUsername(String username);
}
