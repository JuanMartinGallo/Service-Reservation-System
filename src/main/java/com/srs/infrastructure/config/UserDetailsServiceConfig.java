package com.srs.infrastructure.config;

import com.srs.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserDetailsServiceConfig {

    private final UserRepository userRepository;

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return username -> {
            log.debug("Invoking findByUsername in UserDetailsServiceConfig with username: {}", username);
            return userRepository
                    .findByUsername(username)
                    .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                    .map(user -> {
                        List<SimpleGrantedAuthority> authorities = Arrays.stream(user.getRoles().split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                        return new org.springframework.security.core.userdetails.User(
                                user.getUsername(), user.getPassword(), authorities);
                    });
        };
    }
}
