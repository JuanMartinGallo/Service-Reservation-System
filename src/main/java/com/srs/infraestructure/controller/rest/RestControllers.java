package com.srs.infraestructure.controller.rest;

import com.srs.domain.models.dto.AuthResponse;
import com.srs.domain.models.dto.RegisterRequest;
import com.srs.domain.repositories.UserRepository;
import com.srs.domain.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RestControllers {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping(value = "/register")
    public Mono<ServerResponse> register(@RequestBody RegisterRequest request) {
        if (request == null) {
            return ServerResponse.badRequest().build();
        }

        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        AuthResponse message = new AuthResponse("Username is taken!");
                        return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(message);
                    } else {
                        return authService.register(request)
                                .flatMap(authResponse -> ServerResponse.ok().bodyValue(authResponse))
                                .onErrorResume(e -> ServerResponse.status(HttpStatus.BAD_REQUEST).build());
                    }
                })
                .switchIfEmpty(ServerResponse.status(HttpStatus.BAD_REQUEST).build());
    }
}