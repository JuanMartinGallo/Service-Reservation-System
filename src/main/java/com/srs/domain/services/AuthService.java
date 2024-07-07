package com.srs.domain.services;

import com.srs.domain.models.dto.AuthResponse;
import com.srs.domain.models.dto.LoginRequest;
import com.srs.domain.models.dto.RegisterRequest;
import com.srs.domain.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
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
    private final ReactiveAuthenticationManager reactiveAuthenticationManager;
    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final UserServiceImpl userService;

    public Mono<AuthResponse> login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Mono.error(new IllegalArgumentException("Username or password cannot be null"));
        }

        return reactiveAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()))
                .flatMap(auth -> {
                    UserDetails user = (UserDetails) auth.getPrincipal();
                    if (user == null) {
                        return Mono.error(new IllegalStateException("UserDetails cannot be null"));
                    }
                    return jwtService.getToken(user)
                            .map(token -> AuthResponse.builder().token(token).build())
                            .switchIfEmpty(Mono.error(new IllegalStateException("Token cannot be null")));
                })
                .onErrorResume(e -> Mono.error(new BadCredentialsException("Invalid username or password", e)));
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
