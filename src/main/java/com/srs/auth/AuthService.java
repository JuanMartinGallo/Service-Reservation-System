package com.srs.auth;

import com.srs.jwt.JwtService;
import com.srs.model.DTO.RegisterRequest;
import com.srs.model.User;
import com.srs.repository.UserRepository;
import com.srs.service.Impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final UserServiceImpl userService;

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
    User user = userService.mapToEntity(request);

    userRepository.save(user);

    return AuthResponse.builder().token(jwtService.getToken(user)).build();
  }
}
