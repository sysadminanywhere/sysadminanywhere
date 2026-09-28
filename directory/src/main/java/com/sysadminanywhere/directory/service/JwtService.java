package com.sysadminanywhere.directory.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    public String generateToken(String username, List<String> roles, String service) {
        return generateToken(username, roles, service, List.of());
    }

    public String generateToken(String username, List<String> roles, String service, List<String> groups) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("groups", groups)
                .claim("service", service)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateApiToken(String username, String tokenId, List<String> scopes, Date expiresAt) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setId(tokenId)
                .setSubject(username)
                .claim("tokenType", "api")
                .claim("roles", List.of())
                .claim("scopes", scopes)
                .claim("service", "api")
                .setIssuedAt(new Date())
                .setExpiration(expiresAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtPrincipal parseAndValidate(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);
        String service = claims.get("service", String.class);
        String tokenType = claims.get("tokenType", String.class);
        List<String> scopes = claims.get("scopes", List.class);

        return new JwtPrincipal(username, roles == null ? List.of() : roles, service,
                "api".equals(tokenType), claims.getId(), scopes == null ? List.of() : scopes);
    }

    public record JwtPrincipal(String username, List<String> roles, String service,
                               boolean apiToken, String tokenId, List<String> scopes) {}

}
