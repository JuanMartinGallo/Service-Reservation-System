package com.srs.domain.services;

import com.srs.domain.models.User;
import com.srs.domain.models.dto.AuthResponse;
import com.srs.domain.models.dto.LoginRequest;
import com.srs.domain.models.dto.RegisterRequest;
import com.srs.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public Mono<AuthResponse> login(LoginRequest request) {
        return Mono.defer(() ->
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        request.getUsername(),
                                        request.getPassword()
                                )
                        )
                        .flatMap(auth -> {
                            UserDetails user = (UserDetails) auth.getPrincipal();
                            String token = String.valueOf(jwtService.getToken(user));
                            return Mono.just(AuthResponse.builder().token(token).build());
                        })
                        .onErrorResume(e -> Mono.error(new BadCredentialsException("Invalid credentials")))
        );
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        return Mono.defer(() -> {
            User user = mapToEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            return userRepository.save(user)
                    .flatMap(savedUser -> {
                        String token = String.valueOf(jwtService.getToken(savedUser));
                        return Mono.just(AuthResponse.builder().token(token).build());
                    });
        });
    }

    private User mapToEntity(RegisterRequest request) {
        User user = new User();
        user.setFullname(request.getFullname());
        user.setUsername(request.getUsername());
        user.setCountry(request.getCountry());
        user.setPassword(request.getPassword());
        return user;
    }
}