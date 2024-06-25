package com.srs.service.impl;


import com.srs.model.User;
import com.srs.repository.UserRepository;
import com.srs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    /**
     * Retrieves a list of all users.
     *
     * @return a list of User objects representing all users
     */
    @Override
    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a User object based on the given ID.
     *
     * @param id the ID of the User to retrieve
     * @return the User object with the given ID
     */
    @Override
    public Mono<User> getById(final Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
    }

    /**
     * Creates a new user and saves it to the repository.
     *
     * @param user the user object to be created
     * @return a Mono containing the ID of the newly created user
     */
    @Override
    public Mono<Long> createUser(final User user) {
        return userRepository.save(user)
                .map(User::getId);
    }

    /**
     * Updates a user with the given ID.
     *
     * @param id   the ID of the user to be updated
     * @param user the updated user information
     * @return a Mono<Void> indicating completion
     */
    @Override
    public Mono<Void> updateUser(Long id, User user) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(existingUser -> {
                    existingUser.setUsername(user.getUsername());
                    existingUser.setPassword(user.getPassword());
                    existingUser.setFullname(user.getFullname());
                    existingUser.setCountry(user.getCountry());
                    existingUser.setRole(user.getRole());
                    return userRepository.save(existingUser);
                })
                .then();
    }

    /**
     * Deletes a record with the specified ID.
     *
     * @param id the ID of the record to delete
     * @return a Mono<Void> indicating completion
     */
    @Override
    public Mono<Void> deleteUser(final Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(existingUser -> userRepository.delete(existingUser));
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user
     * @return a Mono containing the user with the specified username, or an error if not found
     */
    @Override
    public Mono<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
    }

}
