package com.brother.crm.auth;

import com.brother.crm.entity.Role;
import com.brother.crm.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties properties;
  private final SecretKey key;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String createToken(UserAccount user) {
    Instant now = Instant.now();
    Instant exp = now.plus(properties.getExpireHours(), ChronoUnit.HOURS);
    return Jwts.builder()
        .subject(String.valueOf(user.getId()))
        .claim("account", user.getAccount() == null ? "" : user.getAccount())
        .claim("name", user.getName())
        .claim("phone", user.getPhone() == null ? "" : user.getPhone())
        .claim("role", user.getRole().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  public LoginUser toLoginUser(Claims claims) {
    Long userId = Long.valueOf(claims.getSubject());
    String roleName = claims.get("role", String.class);
    Role role = roleName == null ? Role.ADVISOR : Role.valueOf(roleName);
    return new LoginUser(
        userId,
        claims.get("account", String.class),
        claims.get("name", String.class),
        claims.get("phone", String.class),
        "",
        role,
        true);
  }
}
