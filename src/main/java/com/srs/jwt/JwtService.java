package com.srs.jwt;

import com.srs.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class JwtService {

  private UserRepository userRepository;

  private static final String SECRET_KEY =
    "586E3272357538782F413F4428472B4B6250655368566B597033733676397924";

  /**
   * Generates a token for the given user.
   *
   * @param  user  the user details
   * @return       the generated token
   */
  public String getToken(UserDetails user) throws JwtException {
    try {
      if (userRepository.existsByUsername(user.getUsername())) {
        return getToken(new HashMap<>(), user);
      } else {
        throw new JwtException("User not found");
      }
    } catch (JwtException e) {
      System.out.println(e.getMessage());
    }
    return "";
  }

  /**
   * Generates a token based on the given extra claims and user details.
   *
   * @param  extraClaims  the extra claims to include in the token
   * @param  user         the user details used to set the subject of the token
   * @return              the generated token
   */
  private String getToken(Map<String, Object> extraClaims, UserDetails user) {
    return Jwts
      .builder()
      .setClaims(extraClaims)
      .setSubject(user.getUsername())
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
      .signWith(getKey(), SignatureAlgorithm.HS256)
      .compact();
  }

  /**
   * Generate the key for the function.
   *
   * @return  The generated key.
   */
  private Key getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Retrieves the username from the given token.
   *
   * @param  token  the token from which to retrieve the username
   * @return        the username retrieved from the token
   */
  public String getUsernameFromToken(String token) {
    return getClaim(token, Claims::getSubject);
  }

  /**
   * Checks if the token is valid.
   *
   * @param  token         the token to be validated
   * @param  userDetails  the UserDetails object containing user details
   * @return               true if the token is valid, false otherwise
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    try {
      final String username = getUsernameFromToken(token);
      return (
        username.equals(userDetails.getUsername()) && !isTokenExpired(token)
      );
    } catch (IllegalArgumentException e) {
      System.out.println("Unable to get JWT Token");
      return false;
    }
  }

  /**
   * Retrieves all the claims from the provided token.
   *
   * @param  token  the token from which to retrieve the claims
   * @return        the claims extracted from the token
   */
  private Claims getAllClaims(String token) {
    if (token == null) {
      return null;
    }
    try {
      return Jwts
        .parserBuilder()
        .setSigningKey(getKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
    } catch (ExpiredJwtException e) {
      System.out.println("Token Expired");
    } catch (JwtException e) {
      System.out.println("Invalid Token");
    }
    return null;
  }

  /**
   * A function to get a claim from a token.
   *
   * @param  token            the token from which to get the claim
   * @param  claimsResolver   a function that resolves the claim from the token's claims
   * @return                  the claim resolved by the claimsResolver function
   */
  public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Retrieves the expiration date of a token.
   *
   * @param  token  the token for which to retrieve the expiration date
   * @return        the expiration date of the token
   */
  private Date getExpiration(String token) {
    return getClaim(token, Claims::getExpiration);
  }

  /**
   * Checks if the given token is expired.
   *
   * @param  token  the token to be checked
   * @return        true if the token is expired, false otherwise
   */
  private boolean isTokenExpired(String token) {
    return getExpiration(token).before(new Date());
  }
}
