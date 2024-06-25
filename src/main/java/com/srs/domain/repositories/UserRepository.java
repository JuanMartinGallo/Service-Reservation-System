package com.srs.domain.repositories;

import com.srs.domain.models.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Flux<User> findAll();

    Mono<User> findByUsername(String username);

    Mono<User> findById(int id);

    Mono<Boolean> existsByUsername(String username);
}
