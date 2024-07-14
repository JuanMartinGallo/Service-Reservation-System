package com.srs.domain.services;

import com.srs.domain.models.User;
import com.srs.domain.models.dto.RegisterRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface UserService {
    Flux<User> findAll();

    Mono<User> getById(Long id);

    Mono<Long> createUser(User user);

    Mono<Void> updateUser(Long id, User user);

    Mono<Void> deleteUser(Long id);

    Mono<User> getUserByUsername(String username);

    Mono<User> saveUser(RegisterRequest request);
}