package com.theodo.springblueprint.features.authentication.domain.usecases.recreateusertokens;

import com.theodo.springblueprint.common.domain.ports.RandomGeneratorPort;
import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.features.authentication.domain.ports.TokenClaimsCodecPort;
import com.theodo.springblueprint.features.authentication.domain.ports.UserSessionRepositoryPort;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.services.RefreshTokenService;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;

public class RecreateUserTokensUseCase {

    private final RefreshTokenService refreshTokenService;

    public RecreateUserTokensUseCase(
        final UserSessionRepositoryPort userSessionRepository,
        final TokenClaimsCodecPort tokenClaimsCodec,
        final TokenProperties tokenProperties,
        final TimeProviderPort timeProvider,
        final RandomGeneratorPort randomGenerator) {
        this.refreshTokenService = new RefreshTokenService(
            userSessionRepository,
            tokenClaimsCodec,
            tokenProperties,
            timeProvider,
            randomGenerator
        );
    }

    public UserTokens handle(RecreateUserTokensCommand recreateUserTokensCommand) {
        return refreshTokenService.recreateUserTokens(recreateUserTokensCommand.previousUserToken());
    }
}
