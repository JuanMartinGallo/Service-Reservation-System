package com.srs.domain.repositories;

import com.srs.domain.models.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    @Query("SELECT * FROM users")
    Flux<User> findAll();

    @Query("SELECT * FROM users WHERE username = :username")
    Mono<User> findByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id")
    Mono<User> findById(int id);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username")
    Mono<Boolean> existsByUsername(String username);
}
