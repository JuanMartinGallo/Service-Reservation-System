package com.srs.domain.services;

import com.srs.domain.models.User;
import com.srs.domain.models.dto.AuthResponse;
import com.srs.domain.models.dto.LoginRequest;
import com.srs.domain.models.dto.RegisterRequest;
import com.srs.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<Void> register(RegisterRequest request) {
        log.debug("Starting registration process for user: {}", request.getUsername());

        if (request.getUsername() == null || request.getPassword() == null) {
            log.error("Username or password cannot be null");
            return Mono.error(new IllegalArgumentException("Username or password cannot be null"));
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setFullname(request.getFullname());
        newUser.setCountry(request.getCountry());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRoles(request.getRole());

        return userRepository.save(newUser).then(Mono.empty());
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        log.debug("Starting login process for user: {}", request.getUsername());
        if (request.getUsername() == null || request.getPassword() == null) {
            log.error("Username or password cannot be null");
            return Mono.error(new IllegalArgumentException("Username or password cannot be null"));
        }

        return reactiveUserDetailsService.findByUsername(request.getUsername())
                .flatMap(userDetails -> {
                    if (passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                        log.debug("Authenticated user: {}", userDetails.getUsername());
                        return jwtService.getToken(userDetails)
                                .map(token -> {
                                    log.debug("Generated token for user: {}", token);
                                    return AuthResponse.builder().token(token).build();
                                });
                    } else {
                        return Mono.error(new BadCredentialsException("Invalid username or password"));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error during authentication: {}", e.getMessage());
                    if (e instanceof BadCredentialsException) {
                        return Mono.just(AuthResponse.builder().error("Invalid username or password").build());
                    } else if (e instanceof JwtException) {
                        return Mono.just(AuthResponse.builder().error("JWT error: " + e.getMessage()).build());
                    } else {
                        return Mono.just(AuthResponse.builder().error("Authentication error: " + e.getMessage()).build());
                    }
                });
    }

    public Mono<AuthResponse> refreshToken(String token) {
        return jwtService.getUsernameFromToken(token)
                .flatMap(username -> reactiveUserDetailsService.findByUsername(username)
                        .flatMap(userDetails -> jwtService.isTokenValid(token, userDetails)
                                .flatMap(isValid -> {
                                    if (Boolean.TRUE.equals(isValid)) {
                                        return jwtService.getToken(userDetails)
                                                .map(newToken -> AuthResponse.builder().token(newToken).build());
                                    } else {
                                        return Mono.error(new JwtException("Invalid token"));
                                    }
                                })
                        )
                );
    }

}