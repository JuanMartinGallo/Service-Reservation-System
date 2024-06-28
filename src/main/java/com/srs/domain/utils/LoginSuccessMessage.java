package com.srs.domain.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoginSuccessMessage extends WebFilterChainServerAuthenticationSuccessHandler {

    @Override
    public Mono<Void> onAuthenticationSuccess(
            WebFilterExchange webFilterExchange,
            Authentication authentication
    ) {
        return webFilterExchange.getExchange().getSession()
                .flatMap(session -> {
                    session.getAttributes().put("success", "You have successfully logged in");
                    return webFilterExchange.getChain().filter(webFilterExchange.getExchange());
                });
    }

}

