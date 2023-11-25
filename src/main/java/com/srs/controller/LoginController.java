package com.srs.controller;

import com.srs.auth.AuthResponse;
import com.srs.auth.AuthService;
import com.srs.model.DTO.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoginController {

  private final AuthService authService;
  private final RequestCache requestCache;

  @GetMapping(value = "/login")
  public String login(
    @RequestParam(value = "logout", defaultValue = "false") boolean logout,
    @RequestParam(value = "error", required = false) String error,
    Model model,
    Principal principal,
    RedirectAttributes attribute
  ) {
    if (error != null) {
      model.addAttribute("error", "Invalid username or password");
    }
    if (principal != null) {
      attribute.addFlashAttribute("warning", "You are already logged in");
      return "redirect:/home";
    }
    if (logout) {
      model.addAttribute("message", "You have been logged out");
    }

    return "login";
  }

  @PostMapping(value = "/process-login")
  private String login(
    @RequestBody LoginRequest request,
    HttpServletRequest req,
    HttpServletResponse res
  ) {
    AuthResponse response = authService.login(request);
    if (response.getToken() != null) {
      return loginSuccessHandler(req, res);
    }
    return null;
  }

  private String loginSuccessHandler(
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    SavedRequest savedRequest = requestCache.getRequest(request, response);
    if (savedRequest != null) {
      return "redirect:" + savedRequest.getRedirectUrl();
    }
    return "/";
  }
}
