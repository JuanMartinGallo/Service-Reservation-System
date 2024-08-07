package com.srs.infrastructure.security;

import com.srs.domain.services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
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
    private final JwtAuthManager jwtAuthManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = getTokenFromRequest(exchange);
        if (token == null) {
            return chain.filter(exchange);
        }
        log.debug("Token value in filter: {}", token);

        return jwtService.getUsernameFromToken(token)
                .flatMap(username -> {
                    if (username != null) {
                        return jwtAuthManager.authenticate(new UsernamePasswordAuthenticationToken(username, token))
                                .flatMap(authentication -> {
                                    SecurityContext context = new SecurityContextImpl(authentication);
                                    return chain.filter(exchange)
                                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)))
                                            .doOnEach(signal -> log.debug("SecurityContext after filter: {}", context));
                                });
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