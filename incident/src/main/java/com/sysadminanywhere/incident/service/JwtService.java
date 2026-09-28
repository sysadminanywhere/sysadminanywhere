package com.sysadminanywhere.incident.service;

import com.sysadminanywhere.common.directory.dto.ApiTokenIntrospectionResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    private final RestClient directoryClient;

    @Autowired
    public JwtService(RestClient.Builder restClientBuilder,
                      @Value("${app.services.directory.uri:http://localhost:8081}") String directoryServiceUri) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(2000);
        this.directoryClient = restClientBuilder.clone().baseUrl(directoryServiceUri)
                .requestFactory(requestFactory).build();
    }

    public JwtService() {
        this(RestClient.builder(), "http://localhost:8081");
    }

    public String generateToken(String username, List<String> roles, String service) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("service", service)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
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
        String tokenId = claims.getId();
        boolean apiToken = "api".equals(tokenType);
        if (apiToken) {
            ApiTokenIntrospectionResponse validation = directoryClient.post()
                    .uri("/api/api-tokens/introspect")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(ApiTokenIntrospectionResponse.class);
            if (validation == null || !validation.active() || !Objects.equals(tokenId, validation.tokenId())
                    || !Objects.equals(scopes == null ? List.of() : scopes, validation.scopes())) {
                throw new SecurityException("API token is revoked or invalid");
            }
        }

        return new JwtPrincipal(username, roles == null ? List.of() : roles, service,
                apiToken, tokenId, scopes == null ? List.of() : scopes);
    }

    public record JwtPrincipal(String username, List<String> roles, String service,
                               boolean apiToken, String tokenId, List<String> scopes) {}

}
