package com.theodo.springblueprint.features.authentication.api.endpoints.recreateusertokens;

import com.theodo.springblueprint.features.authentication.api.services.AuthenticationResponseEntity;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.usecases.recreateusertokens.RecreateUserTokensCommand;
import com.theodo.springblueprint.features.authentication.domain.usecases.recreateusertokens.RecreateUserTokensUseCase;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecreateUserTokensEndpoint {

    public static final String URL = "/auth/public/refreshToken";

    private final AuthenticationResponseEntity authenticationResponseEntity;
    private final RecreateUserTokensUseCase recreateUserTokensUseCase;

    public RecreateUserTokensEndpoint(
        final RecreateUserTokensUseCase recreateUserTokensUseCase,
        @Value("${server.servlet.context-path}") final String apiContextPath,
        final TokenProperties tokenProperties) {
        this.authenticationResponseEntity = new AuthenticationResponseEntity(apiContextPath, tokenProperties);
        this.recreateUserTokensUseCase = recreateUserTokensUseCase;
    }

    @PostMapping(URL)
    public ResponseEntity<Void> refreshToken(
        @CookieValue(
            name = AuthenticationResponseEntity.ACCESS_TOKEN_COOKIE_NAME,
            required = false
        ) final String accessToken,
        @CookieValue(
            name = AuthenticationResponseEntity.REFRESH_TOKEN_COOKIE_NAME,
            required = false
        ) final String refreshToken) {
        RecreateUserTokensCommand command = createCommand(refreshToken, accessToken);
        UserTokens userTokens = recreateUserTokensUseCase.handle(command);
        return authenticationResponseEntity.from(userTokens);
    }

    private static RecreateUserTokensCommand createCommand(String refreshToken, String accessToken) {
        return new RecreateUserTokensCommand(
            new UserTokens(new AccessToken(accessToken), new RefreshToken(refreshToken))
        );
    }
}
