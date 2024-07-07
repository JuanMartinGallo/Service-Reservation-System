package com.srs.domain.services;

import com.srs.domain.models.dto.AuthResponse;
import com.srs.domain.models.dto.LoginRequest;
import com.srs.domain.models.dto.RegisterRequest;
import com.srs.domain.services.impl.UserServiceImpl;
import com.srs.infraestructure.security.JwtAuthManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final JwtAuthManager jwtAuthManager;
    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final UserServiceImpl userService;

    public Mono<AuthResponse> login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Mono.error(new IllegalArgumentException("Username or password cannot be null"));
        }

        return jwtAuthManager.authenticate(new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()))
                .flatMap(auth -> {
                    UserDetails user = (UserDetails) auth.getPrincipal();
                    return jwtService.getToken(user)
                            .map(token -> AuthResponse.builder().token(token).build());
                })
                .onErrorResume(e -> {
                    if (e instanceof BadCredentialsException) {
                        return Mono.just(AuthResponse.builder().error("Invalid username or password").build());
                    } else if (e instanceof JwtException) {
                        return Mono.just(AuthResponse.builder().error("JWT error: " + e.getMessage()).build());
                    } else {
                        return Mono.just(AuthResponse.builder().error("Authentication error: " + e.getMessage()).build());
                    }
                });
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null || request.getFullname() == null || request.getCountry() == null) {
            return Mono.error(new IllegalArgumentException("All fields are required"));
        }

        return userService.saveUser(request)
                .flatMap(savedUser -> jwtService.getToken(savedUser)
                        .map(token -> AuthResponse.builder().token(token).build()));
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
