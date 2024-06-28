package com.srs.infraestructure.security;

import com.srs.domain.services.JwtService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

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
                        return ReactiveSecurityContextHolder.getContext()
                                .flatMap(securityContext -> {
                                    if (securityContext.getAuthentication() == null) {
                                        return reactiveUserDetailsService.findByUsername(username)
                                                .flatMap(userDetails -> jwtService.isTokenValid(token, userDetails)
                                                        .filter(valid -> valid)
                                                        .flatMap(valid -> {
                                                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                                                    userDetails,
                                                                    null,
                                                                    userDetails.getAuthorities()
                                                            );

                                                            return Mono.deferContextual(ctx -> Mono.just(ReactiveSecurityContextHolder.withAuthentication(authToken)))
                                                                    .then(chain.filter(exchange));
                                                        }));
                                    } else {
                                        return chain.filter(exchange);
                                    }
                                });
                    } else {
                        return chain.filter(exchange);
                    }
                })
                .switchIfEmpty(chain.filter(exchange));
    }


    private String getTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}