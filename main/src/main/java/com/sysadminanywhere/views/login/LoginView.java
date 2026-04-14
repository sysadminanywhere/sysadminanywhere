package com.sysadminanywhere.views.login;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.AuthService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AnonymousAllowed
@PageTitle("login_view.title")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    @Value("${app.jwt.secret}")
    private String secret;

    private final AuthenticatedUser authenticatedUser;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public LoginView(AuthenticatedUser authenticatedUser, AuthService authService, MessageSource messageSource, LocaleService localeService) {
        this.authenticatedUser = authenticatedUser;
        this.messageSource = messageSource;
        this.localeService = localeService;

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle(getMessage("login_view.title"));
        i18n.getHeader().setDescription(getMessage("login_view.description"));
        i18n.setAdditionalInformation(null);
        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        setOpened(true);

        addLoginListener(event -> {
            try {
                JwtResponse response = authService.authenticate(event.getUsername(), event.getPassword());

                if (response.token() != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            event.getUsername(), null, extractAuthorities(response.token())
                    );

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    VaadinSession.getCurrent().getSession().setAttribute(
                            "SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext()
                    );

                    VaadinSession.getCurrent().setAttribute("jwt_token", response.token());

                    authenticatedUser.save(event.getUsername());

                    getUI().ifPresent(ui -> ui.navigate(""));
                } else {
                    setError(true);
                }
            } catch (Exception e) {
                setError(true);
            }
        });
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            event.forwardTo("");
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> roles = claims.get("roles", List.class);

        if (roles == null) {
            return List.of();
        }

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

}
