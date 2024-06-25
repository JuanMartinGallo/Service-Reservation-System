package com.srs.domain.services;

import com.srs.domain.models.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface UserService {
    Flux<User> findAll();

    Mono<User> getById(final Long id);

    Mono<Long> createUser(final User user);

    Mono<Void> updateUser(final Long id, final User user);

    Mono<Void> deleteUser(final Long id);

    Mono<User> getUserByUsername(String username);
}
