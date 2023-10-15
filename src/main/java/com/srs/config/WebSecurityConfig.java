package com.srs.config;

import com.srs.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

  // private final UserDetailsServiceImpl userDetailsService;
  // private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final String[] SECURITY_IGNORED_ENDPOINTS = {
    "/h2-console/**",
    "/h2/**",
    "/webjars/**",
  };

  // @Override
  //   public void configure(WebSecurity web) {
  //       web.ignoring().antMatchers(SECURITY_IGNORED_ENDPOINTS);
  //   }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    return http
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(AntPathRequestMatcher.antMatcher("/"))
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .formLogin(form -> form.permitAll())
      .logout(logout -> logout.permitAll().logoutSuccessUrl("/"))
      .build();
  }
  // @Bean
  // public void configure(AuthenticationManagerBuilder auth) throws Exception {
  //   auth
  //     .userDetailsService(userDetailsService)
  //     .passwordEncoder(bCryptPasswordEncoder);
  // }
}
