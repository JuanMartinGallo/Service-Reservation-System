package com.srs.infrastructure.security;

import com.srs.domain.services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;
    private final ReactiveUserDetailsService reactiveUserDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .flatMap(auth -> jwtService.getClaims(auth.getCredentials().toString()))
                .onErrorResume(e -> {
                    log.error("Invalid token", e);
                    return Mono.error(new Exception("Invalid token"));
                })
                .flatMap(claims -> {
                    log.debug("Claims: {}", claims);
                    String username = claims.getSubject();
                    Object rolesObj = claims.get("roles");
                    if (rolesObj == null) {
                        log.error("Roles are null for user: {}", username);
                        throw new RuntimeException("Roles are null");
                    }

                    List<String> roles;
                    try {
                        roles = (List<String>) rolesObj;
                    } catch (ClassCastException e) {
                        log.error("Roles format is incorrect: {}", rolesObj, e);
                        throw new RuntimeException("Roles format is incorrect", e);
                    }

                    List<GrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    return reactiveUserDetailsService.findByUsername(username)
                            .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                    userDetails, null, authorities));
                });
    }

}