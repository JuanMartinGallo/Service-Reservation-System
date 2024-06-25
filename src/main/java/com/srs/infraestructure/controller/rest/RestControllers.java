package com.srs.infraestructure.controller.rest;

import com.srs.domain.models.dto.AuthResponse;
import com.srs.domain.services.AuthService;
import com.srs.model.DTO.RegisterRequest;
import com.srs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class RestControllers {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping(value = "/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            AuthResponse message = new AuthResponse("Username is taken!");
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
