package com.srs.auth;

import com.srs.jwt.JwtService;
import com.srs.model.Role;
import com.srs.model.User;
import com.srs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        request.getUsername(),
        request.getPassword()
      )
    );
    UserDetails user = userRepository
      .findByUsername(request.getUsername())
      .orElseThrow();
    String token = jwtService.getToken(user);
    return AuthResponse.builder().token(token).build();
  }

  public AuthResponse register(RegisterRequest request) {
    User user = User
      .builder()
      .fullName(request.getFullName())
      .username(request.getUsername())
      .country(request.getCountry())
      .passwordHash(passwordEncoder.encode(request.getPassword()))
      .role(Role.USER)
      .build();

    userRepository.save(user);

    return AuthResponse.builder().token(jwtService.getToken(user)).build();
  }
}
