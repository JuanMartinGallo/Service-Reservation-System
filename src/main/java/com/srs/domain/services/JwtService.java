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
import reactor.core.scheduler.Schedulers;

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

    /**
     * Generates a token for the given user.
     *
     * @param user the user details
     * @return the generated token
     */
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

    /**
     * Generates a token based on the given extra claims and user details.
     *
     * @param extraClaims the extra claims to include in the token
     * @param user        the user details used to set the subject of the token
     * @return the generated token
     */
    private String generateToken(Map<String, Object> extraClaims, UserDetails user) {
        Key key = getKey();
        String jwt = Jwts.builder()
                .subject(user.getUsername())
                .claims(extraClaims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(key)
                .compact()
                .replaceAll("\\s", "");

        log.debug("Generated JWT: {}", jwt);
        return jwt;
    }

    /**
     * Generates the key for the function.
     *
     * @return The generated key.
     */
    public SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey.trim());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Checks if the token is valid.
     *
     * @param token       the token to be validated
     * @param userDetails the UserDetails object containing user details
     * @return "true" if the token is valid, false otherwise
     */
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

    /**
     * Retrieves all the claims from the provided token.
     *
     * @param token the token from which to retrieve the claims
     * @return the claims extracted from the token
     */
    private Mono<Claims> getClaims(String token) {
        return Mono.fromCallable(() -> {
            if (token == null) {
                return null;
            }
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * A function to get a claim from a token.
     *
     * @param token          the token from which to get the claim
     * @param claimsResolver a function that resolves the claim from the token's claims
     * @return the claim resolved by the claimsResolver function
     */
    public <T> Mono<T> getClaim(String token, Function<Claims, T> claimsResolver) {
        return getClaims(token)
                .flatMap(claims -> claims != null ? Mono.just(claimsResolver.apply(claims)) : Mono.empty());
    }

    /**
     * Retrieves the expiration date of a token.
     *
     * @param token the token for which to retrieve the expiration date
     * @return the expiration date of the token
     */
    private Mono<Date> getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if the given token is expired.
     *
     * @param token the token to be checked
     * @return "true" if the token is expired, false otherwise
     */
    private Mono<Boolean> isTokenExpired(String token) {
        return getExpiration(token)
                .map(expiration -> expiration.before(new Date()));
    }

    /**
     * Retrieves the username from the given token.
     *
     * @param token the token from which to retrieve the username
     * @return the username retrieved from the token
     */
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