package com.srs.config;

import com.srs.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final AuthenticationProvider authProvider;

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
        login.permitAll();
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
