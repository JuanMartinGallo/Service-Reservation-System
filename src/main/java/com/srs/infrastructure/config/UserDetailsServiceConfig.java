package com.srs.infrastructure.config;

import com.srs.domain.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class UserDetailsServiceConfig {

    private final UserRepository userRepository;

    public UserDetailsServiceConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return username -> {
            log.debug("Invoking findByUsername in UserDetailsServiceConfig with username: {}", username);
            return userRepository
                    .findByUsername(username)
                    .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                    .cast(UserDetails.class);
        };
    }
}

