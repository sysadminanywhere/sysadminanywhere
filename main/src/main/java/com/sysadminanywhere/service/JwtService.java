package com.sysadminanywhere.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    public JwtPrincipal parseAndValidate(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

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