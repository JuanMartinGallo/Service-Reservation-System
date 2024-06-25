package com.srs.infraestructure.controller.config;

import com.srs.infraestructure.security.JwtAuthenticationFilter;
import com.srs.util.LoginSuccessMessage;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;
    private final LoginSuccessMessage loginSuccessMessage;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        return http
                .csrf(crsf -> crsf.disable())
                .headers(headers ->
                        headers.frameOptions(frameOptions -> frameOptions.disable())
                )
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(
                                        PathRequest.toStaticResources().atCommonLocations(),
                                        new AntPathRequestMatcher("/auth/**"),
                                        new AntPathRequestMatcher("/h2-console/**"),
                                        new AntPathRequestMatcher("/"),
                                        new AntPathRequestMatcher("/index"),
                                        new AntPathRequestMatcher("/home")
                                )
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .formLogin(login -> {
                    login
                            .loginPage("/login")
                            .successHandler(loginSuccessMessage)
                            .loginProcessingUrl("/process-login")
                            .defaultSuccessUrl("/home", true)
                            .failureUrl("/login?error=true")
                            .permitAll();
                })
                .logout(logout -> {
                    logout
                            .logoutSuccessUrl("/login?logout=true")
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll();
                })
                .sessionManagement(sessionManagement -> {
                    sessionManagement
                            .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                            .invalidSessionUrl("/login")
                            .maximumSessions(1)
                            .expiredUrl("/login");
                    sessionManagement.sessionFixation().migrateSession();
                })
                .authenticationProvider(authProvider)
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}
