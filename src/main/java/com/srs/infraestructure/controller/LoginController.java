package com.srs.infraestructure.controller;

import com.srs.domain.models.dto.LoginRequest;
import com.srs.domain.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.srs.domain.utils.ApplicationConstants.LOGIN;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;

    @GetMapping(value = "/login")
    public Mono<String> login(
            @RequestParam(value = "logout", defaultValue = "false") boolean logout,
            @RequestParam(value = "error", required = false) String error,
            ServerWebExchange exchange,
            Model model
    ) {
        log.debug("GET /login called with logout={} and error={}", logout, error);
        return exchange.getSession()
                .flatMap(session -> {
                    if (error != null) {
                        model.addAttribute("error", "Invalid username or password");
                        log.debug("Invalid username or password");
                        return Mono.just(LOGIN);
                    }
                    return exchange.getPrincipal()
                            .flatMap(principalObj -> {
                                if (principalObj != null) {
                                    model.addAttribute("warning", "You are already logged in");
                                    log.debug("User is already logged in");
                                    return Mono.just(LOGIN);
                                }
                                if (logout) {
                                    model.addAttribute("message", "You have been logged out");
                                    log.debug("User has been logged out");
                                    return Mono.just(LOGIN);
                                }
                                log.debug("Returning login page");
                                return Mono.just(LOGIN);
                            });
                });
    }

    @PostMapping(value = "/process-login")
    public Mono<String> login(@ModelAttribute LoginRequest request, ServerWebExchange exchange) {
        log.debug("POST /process-login called with request={}", request);
        return authService.login(request)
                .flatMap(response -> {
                    if (response.getToken() != null) {
                        log.debug("Login successful, token received");
                        return exchange.getSession()
                                .doOnNext(session -> {
                                    session.getAttributes().put("token", response.getToken());
                                    log.debug("Token added to session attributes");
                                })
                                .then(Mono.just("redirect:/home"));
                    }
                    log.debug("Login failed, no token received");
                    return Mono.just("redirect:/login?error=true");
                });
    }
}
