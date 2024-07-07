package com.srs.infraestructure.security;

import com.srs.domain.services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .flatMap(auth -> jwtService.getClaims(auth.getCredentials().toString())
                        .map(claims -> {
                            String username = claims.getSubject();
                            String role = claims.get("role", String.class);
                            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                                    .username(username)
                                    .password("")
                                    .authorities(authorities)
                                    .build();
                            return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                        })
                        .onErrorResume(e -> Mono.error(new JwtException("Invalid token", e)))
                );
    }
}
