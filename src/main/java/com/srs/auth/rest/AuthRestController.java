package com.srs.auth.rest;

import com.srs.jwt.JwtService;
import com.srs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthRestController {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  @GetMapping(value = "/jwt/{id}")
  public ResponseEntity<?> getJwt(@PathVariable String id) {
    try {
      UserDetails user = userRepository
        .findById(Integer.parseInt(id))
        .orElseThrow();
      return ResponseEntity.ok(jwtService.getToken(user));
    } catch (Exception e) {
      return new ResponseEntity<String>(
        "Ha ocurrido un error: " + e.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }
}
