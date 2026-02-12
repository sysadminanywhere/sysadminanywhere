package com.sysadminanywhere.service;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Service
public class JwtService {

    private static final String SECRET = "MySuperSecretKeyForJWTValidation123456";

    private final Key key;

    public JwtService() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Валидирует JWT и возвращает principal с username и ролями
     */
    public JwtPrincipal parseAndValidate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);

        return new JwtPrincipal(username, roles);
    }

    public record JwtPrincipal(String username, List<String> roles) {}

}