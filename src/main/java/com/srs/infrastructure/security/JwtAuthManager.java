package com.srs.infrastructure.security;

import com.srs.domain.services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        log.debug("Starting authentication for token: {}", token);

        return jwtService.getUsernameFromToken(token)
                .map(username -> new UsernamePasswordAuthenticationToken(username, token, new ArrayList<>()))
                .cast(Authentication.class)
                .onErrorResume(e -> {
                    log.error("Error during token authentication: {}", e.getMessage());
                    return Mono.error(new BadCredentialsException("Invalid token"));
                });
    }
}

