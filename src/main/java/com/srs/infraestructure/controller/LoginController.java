package com.srs.infraestructure.controller;

import com.srs.domain.models.dto.LoginRequest;
import com.srs.domain.services.AuthService;
import com.srs.domain.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static com.srs.domain.utils.ApplicationConstants.LOGIN;


@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;
    private final JwtService jwtService;

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
        log.debug("Starting login process for user {}", request.getUsername());
        return authService.login(request)
                .flatMap(response -> {
                    if (response.getToken() != null) {
                        log.debug("Token generated for user {}: {}", request.getUsername(), response.getToken());
                        return exchange.getSession()
                                .flatMap(session -> {
                                    String token = response.getToken().replaceAll("\\s", ""); // Eliminar espacios en blanco
                                    log.debug("Token before saving to session: '{}'", token);
                                    session.getAttributes().put("token", token);
                                    return saveSecurityContextToSession(token, session);
                                })
                                .then(Mono.just("redirect:/home"));
                    }
                    log.debug("Error generating token for user {}", request.getUsername());
                    return Mono.just("redirect:/login?error=true");
                });
    }

    private Mono<Void> saveSecurityContextToSession(String token, WebSession session) {
        log.debug("Saving security context to session {}", token);
        Authentication authentication = getAuthenticationFromToken(token);

        SecurityContextImpl securityContext = new SecurityContextImpl(authentication);

        session.getAttributes().put("SPRING_SECURITY_CONTEXT", securityContext);

        return Mono.empty();
    }

    private Authentication getAuthenticationFromToken(String token) {
        log.debug("Token received for authentication: '{}'", token);
        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(jwtService.getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("Error parsing JWT: ", e);
            throw new IllegalArgumentException("Token JWT is not valid", e);
        }

        String username = claims.getSubject();

        Object rolesObject = claims.get("role");

        List<String> roles;
        if (rolesObject instanceof List<?>) {
            roles = ((List<?>) rolesObject).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        } else {
            throw new IllegalArgumentException("Roles must be a list of strings");
        }

        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("")
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}
