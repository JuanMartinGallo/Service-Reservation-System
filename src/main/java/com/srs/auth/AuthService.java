package com.srs.auth;

import com.srs.jwt.JwtService;
import com.srs.model.DTO.LoginRequest;
import com.srs.model.DTO.RegisterRequest;
import com.srs.model.User;
import com.srs.repository.UserRepository;
import com.srs.service.impl.UserServiceImpl;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final UserServiceImpl userService;

  /**
   * Authenticates a user's login request and returns an authentication response.
   *
   * @param  request  the login request containing the username and password
   * @return          the authentication response containing the JWT token
   */
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

  /**
   * Registers a new user and returns an authentication response.
   *
   * @param  request   the registration request containing user details
   * @return           the authentication response with a generated token
   */
  public AuthResponse register(RegisterRequest request) {
    User user = userService.mapToEntity(request);

    userRepository.save(user);

    return AuthResponse.builder().token(jwtService.getToken(user)).build();
  }
}
