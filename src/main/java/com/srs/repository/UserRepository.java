package com.srs.repository;

import com.srs.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  User findUserByUsername(String username);

  Optional<User> findByUsername(String username);

  Optional<User> findById(int id);

  Boolean existsByUsername(String username);

  
}
