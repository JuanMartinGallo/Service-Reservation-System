package com.srs.infraestructure.controller.rest;

import com.srs.domain.repositories.UserRepository;
import com.srs.domain.services.JwtService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
@Validated
public class AuthRestController {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping(value = "/jwt/{id}")
    public Mono<ResponseEntity<String>> getJwt(@Valid @PathVariable String id, BindingResult result) {
        if (result.hasErrors()) {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Validation failed: " + result.getAllErrors()));
        }

        try {
            return userRepository.findById(Integer.parseInt(id))
                    .flatMap(user -> {
                        String token = String.valueOf(jwtService.getToken(user));
                        return Mono.just(ResponseEntity.ok().body(token));
                    })
                    .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
                    .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error getting user: " + e.getMessage())));
        } catch (NumberFormatException e) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid id: " + id));
        }
    }
}

