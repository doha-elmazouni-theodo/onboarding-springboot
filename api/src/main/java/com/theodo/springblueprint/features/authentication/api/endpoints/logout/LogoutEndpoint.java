package com.theodo.springblueprint.features.authentication.api.endpoints.logout;

import com.theodo.springblueprint.features.authentication.api.services.AuthenticationResponseEntity;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.usecases.logout.LogoutUseCase;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutEndpoint {

    public static final String URL = "/auth/logout";

    private final LogoutUseCase logoutUseCase;
    private final AuthenticationResponseEntity authenticationResponseEntity;

    public LogoutEndpoint(
        final LogoutUseCase logoutUseCase,
        @Value("${server.servlet.context-path}") final String apiContextPath,
        final TokenProperties tokenProperties) {
        this.logoutUseCase = logoutUseCase;
        this.authenticationResponseEntity = new AuthenticationResponseEntity(apiContextPath, tokenProperties);
    }

    @PostMapping(URL)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
        @CookieValue(
            name = AuthenticationResponseEntity.REFRESH_TOKEN_COOKIE_NAME,
            required = false
        ) final String refreshToken) {
        logoutUseCase.handle(new RefreshToken(refreshToken));
        return authenticationResponseEntity.expireAuthCookies();
    }
}
