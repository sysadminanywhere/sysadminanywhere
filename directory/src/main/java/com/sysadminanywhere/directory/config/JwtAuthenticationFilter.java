package com.sysadminanywhere.directory.config;

import com.sysadminanywhere.directory.service.JwtService;
import com.sysadminanywhere.directory.service.ApiTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ApiTokenService apiTokenService;

    public JwtAuthenticationFilter(JwtService jwtService, ApiTokenService apiTokenService) {
        this.jwtService = jwtService;
        this.apiTokenService = apiTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            try {
                var principal = jwtService.parseAndValidate(token);
                if (principal.apiToken() && !apiTokenService.isActive(token, principal.tokenId(), principal.scopes())) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                var authorities = principal.roles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                if (principal.apiToken()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_API_TOKEN"));
                    principal.scopes().stream().map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                            .forEach(authorities::add);
                }

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal.username(),
                        token,
                        authorities
                );

                // service-контекст понадобится для раздельных Vault/LDAP ключей
                authentication.setDetails(principal.service());

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
