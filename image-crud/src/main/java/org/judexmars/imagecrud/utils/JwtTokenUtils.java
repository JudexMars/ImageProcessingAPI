package org.judexmars.imagecrud.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.judexmars.imagecrud.config.security.JwtProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility class for working with JWT tokens.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenUtils {

  private final JwtProperties jwtProperties;

  private SecretKey getSecretKey(String secret) {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  private String generateToken(UserDetails userDetails, Duration lifetime, String secret) {
    Date issuedDate = new Date();
    Date expirationDate = new Date(issuedDate.getTime() + lifetime.toMillis());
    Map<String, Object> claims = new HashMap<>();
    List<String> roles =
        userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    claims.put("roles", roles);

    return Jwts.builder()
        .claims(claims)
        .subject(userDetails.getUsername())
        .issuedAt(issuedDate)
        .expiration(expirationDate)
        .signWith(getSecretKey(secret), Jwts.SIG.HS256)
        .compact();
  }

  public String generateAccessToken(UserDetails userDetails) {
    return generateToken(userDetails, jwtProperties.getAccessLifetime(),
        jwtProperties.getAccessSecret());
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return generateToken(userDetails, jwtProperties.getRefreshLifetime(),
        jwtProperties.getRefreshSecret());
  }

  private Claims getAllClaimsFromToken(String token, SecretKey secretKey) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public Date getExpirationDateFromRefreshToken(String token) {
    return getAllClaimsFromToken(token,
        getSecretKey(jwtProperties.getRefreshSecret())).getExpiration();
  }

  public String getUsernameFromAccessToken(String token) {
    return getAllClaimsFromToken(token, getSecretKey(jwtProperties.getAccessSecret())).getSubject();
  }

  public String getUsernameFromRefreshToken(String token) {
    return getAllClaimsFromToken(token,
        getSecretKey(jwtProperties.getRefreshSecret())).getSubject();
  }

  @SuppressWarnings("unchecked")
  public List<String> getRolesFromAccessToken(String token) {
    return getAllClaimsFromToken(token, getSecretKey(jwtProperties.getAccessSecret())).get("roles",
        List.class);
  }
}
