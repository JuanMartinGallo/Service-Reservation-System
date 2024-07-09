package com.srs.infrastructure.security;

import com.srs.domain.services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private final JwtService jwtService;
    private final ReactiveUserDetailsService reactiveUserDetailsService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = getTokenFromRequest(exchange);
        if (token == null) {
            return chain.filter(exchange);
        }

        return jwtService.getUsernameFromToken(token)
                .flatMap(username -> {
                    if (username != null) {
                        return reactiveUserDetailsService.findByUsername(username)
                                .flatMap(userDetails -> jwtService.isTokenValid(token, userDetails)
                                        .filter(valid -> valid)
                                        .flatMap(valid -> {
                                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                                    userDetails, null, userDetails.getAuthorities());

                                            return chain.filter(exchange)
                                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authToken));
                                        }));
                    } else {
                        return chain.filter(exchange);
                    }
                })
                .doOnError(e -> log.error("Error in JWT authentication in filter: {}", e.getMessage()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Invalid token");
                    return Mono.empty();
                }));
    }

    private String getTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}