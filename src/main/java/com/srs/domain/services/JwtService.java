package com.srs.domain.services;

import com.srs.domain.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.srs.domain.utils.ApplicationConstants.USER_NOT_FOUND;

@Slf4j
@Service
public class JwtService {

    private final UserRepository userRepository;

    @Value("${jwt.secret.key}")
    private String secretKey;

    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<String> getToken(UserDetails user) {
        return userRepository.existsByUsername(user.getUsername())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        Map<String, Object> extraClaims = new HashMap<>();
                        extraClaims.put("role", user.getAuthorities().iterator().next().getAuthority());
                        return Mono.just(generateToken(extraClaims, user));
                    } else {
                        return Mono.error(new JwtException(USER_NOT_FOUND));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error generating token: {}", e.getMessage());
                    return Mono.error(new JwtException("Error generating token", e));
                });
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails user) {
        Key key = getKey();
        long validityInMilliseconds = 3600000;
        String jwt = Jwts.builder()
                .subject(user.getUsername())
                .claims(extraClaims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + validityInMilliseconds))
                .signWith(key)
                .compact();

        log.debug("Generated JWT: {} for the user: {}", jwt, user.getUsername());
        return jwt;
    }

    public SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey.trim());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Mono<Boolean> isTokenValid(String token, UserDetails userDetails) {
        return getUsernameFromToken(token)
                .flatMap(username -> {
                    if (username == null) {
                        return Mono.just(false);
                    }
                    return isTokenExpired(token)
                            .map(isExpired -> username.equals(userDetails.getUsername()) && !isExpired);
                })
                .onErrorResume(e -> {
                    log.error("Unable to get JWT Token", e);
                    return Mono.just(false);
                });
    }

    public Mono<Claims> getClaims(String token) {
        try {
            log.debug("Validating token: {}", token);
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            log.debug("Token valid, claims: {}", claims);
            return Mono.just(claims);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid token: {}", e.getMessage());
            return Mono.error(new JwtException("Invalid token", e));
        }
    }

    public <T> Mono<T> getClaim(String token, Function<Claims, T> claimsResolver) {
        return getClaims(token)
                .flatMap(claims -> claims != null ? Mono.just(claimsResolver.apply(claims)) : Mono.empty());
    }

    private Mono<Date> getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    private Mono<Boolean> isTokenExpired(String token) {
        return getExpiration(token)
                .map(expiration -> expiration.before(new Date()));
    }

    public Mono<String> getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject)
                .onErrorResume(e -> {
                    log.error("Error extracting username from token: {}", e.getMessage());
                    return Mono.error(new JwtException("Error extracting username from token", e));
                });
    }

    public Mono<String> getRoleFromToken(String token) {
        return getClaim(token, claims -> claims.get("role", String.class))
                .onErrorResume(e -> {
                    log.error("Error extracting role from token: {}", e.getMessage());
                    return Mono.error(new JwtException("Error extracting role from token", e));
                });
    }

}