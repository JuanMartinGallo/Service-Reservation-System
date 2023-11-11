package com.srs.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  // @GetMapping(value = "/login")//TODO: editar el login.html
  // public String login() {
  //   return "login";
  // }

  // @PostMapping(value = "/login")
  // public String login(@RequestBody LoginRequest request) {
  //   AuthResponse response = authService.login(request);
  //   if (response.getToken() != null) {
  //     return "index";
  //   } else {
  //     return "Error";
  //   }
  // }
}
