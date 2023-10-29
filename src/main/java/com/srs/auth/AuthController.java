package com.srs.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  //TODO: ver como iniciar sesion
  @PostMapping(value = "login")
  public String login(@RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    if (response.getToken() != null) {
      return "Redirect:/Index";
    } else {
      return "Error";
    }
  }

  @PostMapping(value = "register")
  public ResponseEntity<AuthResponse> register(
    @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(authService.register(request));
  }
}
