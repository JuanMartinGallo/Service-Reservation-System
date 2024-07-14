package com.srs.domain.services.impl;

import com.srs.domain.models.User;
import com.srs.domain.models.dto.RegisterRequest;
import com.srs.domain.repositories.UserRepository;
import com.srs.domain.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.srs.domain.utils.ApplicationConstants.USER_NOT_FOUND;
import static com.srs.domain.utils.ApplicationUtils.mapToEntity;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Mono<User> getById(final Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND)));
    }

    @Override
    public Mono<Long> createUser(final User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user)
                .map(User::getId);
    }

    @Override
    public Mono<Void> updateUser(Long id, User user) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND)))
                .flatMap(existingUser -> {
                    existingUser.setUsername(user.getUsername());
                    existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    existingUser.setFullname(user.getFullname());
                    existingUser.setCountry(user.getCountry());
                    existingUser.setRoles(user.getRoles());
                    existingUser.setReservations(user.getReservations());
                    return userRepository.save(existingUser);
                })
                .then();
    }


    @Override
    public Mono<Void> deleteUser(final Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND)))
                .flatMap(userRepository::delete);
    }

    @Override
    public Mono<User> getUserByUsername(String username) {
        log.debug("Invoking findByUsername in UserServiceImpl with username: {}", username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND)));
    }

    @Override
    public Mono<User> saveUser(RegisterRequest request) {
        User user = mapToEntity(request, passwordEncoder);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }
}
