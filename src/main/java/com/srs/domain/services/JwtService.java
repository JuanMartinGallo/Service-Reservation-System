package com.srs.domain.services;

import com.srs.domain.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.srs.domain.utils.ApplicationConstants.USER_NOT_FOUND;

@Slf4j
@Service
public class JwtService {

    private final UserRepository userRepository;

    @Value("${jwt.secret.key}")
    private String secretKey;
    @Value("${jwt.expiration.time}")
    private long expirationTime;

    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<String> getToken(UserDetails userDetails) {
        return userRepository.existsByUsername(userDetails.getUsername())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        Map<String, Object> extraClaims = new HashMap<>();
                        extraClaims.put("roles", userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()));
                        return Mono.just(generateToken(extraClaims, userDetails));
                    } else {
                        return Mono.error(new JwtException(USER_NOT_FOUND));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error generating token: {}", e.getMessage());
                    return Mono.error(new JwtException("Error generating token", e));
                });
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Key key = getKey();
        String jwt = Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(extraClaims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();

        log.debug("Generated JWT: {} for the user: {}", jwt, userDetails.getUsername());
        return jwt;
    }

    public SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey.trim());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Authentication getAuthenticationFromToken(String token) {
        log.debug("Token received for authentication: '{}'", token);
        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (org.springframework.security.oauth2.jwt.JwtException e) {
            log.error("Error parsing JWT: ", e);
            throw new IllegalArgumentException("Token JWT is not valid", e);
        }

        String username = claims.getSubject();

        Object rolesObject = claims.get("role");

        List<String> roles;
        if (rolesObject instanceof List<?>) {
            roles = ((List<?>) rolesObject).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        } else {
            throw new IllegalArgumentException("Roles must be a list of strings");
        }

        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("")
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
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
            log.debug("Validating token to extract claims: {}", token);
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
        return getClaim(token, Claims::getExpiration)
                .onErrorResume(e -> {
                    log.error("Error extracting expiration from token: {}", e.getMessage());
                    return Mono.error(new JwtException("Error extracting expiration from token", e));
                });
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
}

