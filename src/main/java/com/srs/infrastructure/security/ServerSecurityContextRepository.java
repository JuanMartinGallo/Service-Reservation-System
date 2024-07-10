package com.srs.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServerSecurityContextRepository extends WebSessionServerSecurityContextRepository {

    private final JwtAuthManager jwtAuthManager;

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    String token = session.getAttribute("token");
                    if (token != null) {
                        log.debug("Token in session: {}", token);
                        return jwtAuthManager.authenticate(new UsernamePasswordAuthenticationToken(token, token))
                                .map(SecurityContextImpl::new);
                    } else
                        log.debug("No token in session");
                    return Mono.empty();
                });
    }
}

