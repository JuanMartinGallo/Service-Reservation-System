package com.srs.infraestructure.config;

import com.srs.infraestructure.security.JwtAuthManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthManager jwtAuthManager;

    @Value("${login.path}")
    private String loginPath;

    @Value("${logout.path}")
    private String logoutPath;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ServerRequestCache requestCache) {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtAuthManager);
        authenticationWebFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/process-login"));
        authenticationWebFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        authenticationWebFilter.setAuthenticationFailureHandler(authenticationFailureHandler());

        RedirectServerLogoutSuccessHandler logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create(loginPath));

        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/", "/index", "/home", "/webjars/**", "/css/**", "/js/**", "/images/**", loginPath, logoutPath, "/auth/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/process-login").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .formLogin(form -> form
                        .loginPage(loginPath)
                        .authenticationSuccessHandler(authenticationSuccessHandler())
                        .authenticationFailureHandler(authenticationFailureHandler())
                )
                .logout(logout -> logout
                        .logoutUrl(logoutPath)
                        .logoutSuccessHandler(logoutSuccessHandler)
                )
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(redirectServerAuthenticationEntryPoint(requestCache))
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        return (webFilterExchange, authentication) -> {
            ServerWebExchange exchange = webFilterExchange.getExchange();
            return Mono.fromRunnable(() -> {
                exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                exchange.getResponse().getHeaders().setLocation(URI.create("/home"));
            });
        };
    }

    @Bean
    public ServerAuthenticationFailureHandler authenticationFailureHandler() {
        return (webFilterExchange, exception) -> {
            ServerWebExchange exchange = webFilterExchange.getExchange();
            return Mono.fromRunnable(() -> {
                exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                exchange.getResponse().getHeaders().setLocation(URI.create("/login?error=true"));
            });
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ServerRequestCache requestCache() {
        return NoOpServerRequestCache.getInstance();
    }

    @Bean
    public RedirectServerAuthenticationEntryPoint redirectServerAuthenticationEntryPoint(ServerRequestCache serverRequestCache) {
        RedirectServerAuthenticationEntryPoint entryPoint = new RedirectServerAuthenticationEntryPoint(loginPath);
        entryPoint.setRequestCache(serverRequestCache);
        return entryPoint;
    }
}