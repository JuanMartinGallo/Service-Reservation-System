package com.srs.infraestructure.config;

import com.srs.domain.repositories.UserRepository;
import com.srs.domain.utils.LoginSuccessMessage;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import static com.srs.domain.utils.ApplicationConstants.USER_NOT_FOUND;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers(HttpMethod.GET, "/", "/index", "/home").permitAll()
                                .pathMatchers("/auth/**", "/h2-console/**").permitAll()
                                .anyExchange().authenticated()
                )
                .formLogin(form ->
                        form
                                .loginPage("/login")
                                .authenticationSuccessHandler(loginSuccessMessage())
                                .authenticationFailureHandler((exchange, exception) -> Mono.empty())
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessHandler(new RedirectServerLogoutSuccessHandler())
                )
                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(new RedirectServerAuthenticationEntryPoint("/login"))
                )
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public LoginSuccessMessage loginSuccessMessage() {
        return new LoginSuccessMessage();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return username -> userRepository
                .findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(USER_NOT_FOUND)))
                .cast(UserDetails.class);
    }

}

