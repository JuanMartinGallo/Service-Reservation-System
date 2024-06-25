package com.srs.domain.services;

import com.srs.domain.models.User;
import com.srs.domain.models.dto.AuthResponse;
import com.srs.domain.models.dto.LoginRequest;
import com.srs.domain.models.dto.RegisterRequest;
import com.srs.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.srs.domain.utils.ApplicationUtils.mapToEntity;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticates a user's login request and returns an authentication response.
     *
     * @param request the login request containing the username and password
     * @return a Mono emitting the authentication response containing the JWT token
     */
    public Mono<AuthResponse> login(LoginRequest request) {
        return Mono.fromCallable(() -> {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtService.getToken(user);
            return AuthResponse.builder().token(token).build();
        });
    }

    /**
     * Registers a new user and returns an authentication response.
     *
     * @param request the registration request containing user details
     * @return a Mono emitting the authentication response with a generated token
     */
    public Mono<AuthResponse> register(RegisterRequest request) {
        return Mono.fromCallable(() -> {
            User user = mapToEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            userRepository.save(user);

            String token = jwtService.getToken(user);
            return AuthResponse.builder().token(token).build();
        });
    }
}

