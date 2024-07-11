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

import static com.srs.domain.utils.ApplicationConstants.TOKEN;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServerSecurityContextRepository extends WebSessionServerSecurityContextRepository {

    private final JwtAuthManager jwtAuthManager;

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();

        if (path.startsWith(" ")) {
            path = path.trim();
        }

        log.debug("Invoking load method for path: {}", path);

        String finalPath = path;
        return Mono.defer(() -> {
            Mono<SecurityContext> cachedSecurityContext = exchange.getAttribute("cachedSecurityContext");
            if (cachedSecurityContext != null) {
                log.debug("Returning cached SecurityContext for path: {}", finalPath);
                return cachedSecurityContext;
            } else {
                return exchange.getSession()
                        .flatMap(session -> {
                            String token = session.getAttribute(TOKEN);
                            if (token != null) {
                                log.debug("Token in session: {}", token);
                                return jwtAuthManager.authenticate(new UsernamePasswordAuthenticationToken(token, token))
                                        .map(SecurityContextImpl::new)
                                        .doOnNext(context -> exchange.getAttributes().put("cachedSecurityContext", Mono.just(context)))
                                        .doOnError(e -> log.error("Error authenticating token: {}", e.getMessage()));
                            } else {
                                log.debug("No token in session for path: {}", finalPath);
                                return Mono.empty();
                            }
                        });
            }
        });
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return exchange.getSession()
                .doOnNext(session -> {
                    if (context.getAuthentication() != null) {
                        String token = (String) context.getAuthentication().getCredentials();
                        log.debug("Saving token to session: {}", token);
                        session.getAttributes().put(TOKEN, token);
                    } else {
                        log.debug("Clearing token from session");
                        session.getAttributes().remove(TOKEN);
                    }
                })
                .then();
    }

}

