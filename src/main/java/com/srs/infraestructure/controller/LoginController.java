package com.srs.infraestructure.controller;

import com.srs.domain.models.dto.LoginRequest;
import com.srs.domain.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;

    @GetMapping(value = "/login")
    public Mono<String> login(
            @RequestParam(value = "logout", defaultValue = "false") boolean logout,
            @RequestParam(value = "error", required = false) String error,
            ServerWebExchange exchange
    ) {
        return exchange.getSession()
                .flatMap(session -> {
                    if (error != null) {
                        session.getAttributes().put("error", "Invalid username or password");
                    }
                    return exchange.getPrincipal()
                            .flatMap(principalObj -> {
                                if (principalObj != null) {
                                    session.getAttributes().put("warning", "You are already logged in");
                                    return Mono.just("redirect:/home");
                                }
                                if (logout) {
                                    session.getAttributes().put("message", "You have been logged out");
                                }
                                return Mono.just("login");
                            });
                });
    }

    @PostMapping(value = "/process-login")
    public Mono<String> login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request)
                .flatMap(response -> {
                    if (response.getToken() != null) {
                        return Mono.just("redirect:/login-success");
                    }
                    return Mono.just("redirect:/login?error=true");
                });
    }

    @GetMapping("/login-success")
    public Mono<String> loginSuccessHandler(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    String redirectUrl = exchange.getRequest().getURI().getPath();
                    return Mono.justOrEmpty(redirectUrl)
                            .switchIfEmpty(Mono.just("/"));
                });
    }
}