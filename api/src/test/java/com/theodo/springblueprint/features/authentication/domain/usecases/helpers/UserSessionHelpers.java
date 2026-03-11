package com.theodo.springblueprint.features.authentication.domain.usecases.helpers;

import static com.theodo.springblueprint.testhelpers.fixtures.SimpleFixture.aRefreshToken;
import static com.theodo.springblueprint.testhelpers.helpers.Mapper.toUserPrincipal;
import static java.util.Objects.requireNonNullElse;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.entities.UserSessionBuildExecutor;
import com.theodo.springblueprint.features.authentication.domain.entities.UserSessionBuildExecutors;
import com.theodo.springblueprint.features.authentication.domain.ports.UserSessionRepositoryPort;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.testhelpers.annotations.ExecutableBuilder;
import jakarta.annotation.Nullable;
import java.time.Instant;

public interface UserSessionHelpers {
    UserSessionRepositoryPort userSessionRepository();
    TimeProviderPort timeProvider();
    TokenProperties tokenProperties();

    default UserSessionBuildExecutors.Optionals createUserSession(User user) {
        return UserSessionBuildExecutor.start()
            .user(user)
            .userSessionRepository(userSessionRepository())
            .timeProvider(timeProvider())
            .tokenProperties(tokenProperties());
    }

    class ExecutableBuilders {

        @ExecutableBuilder
        public static UserSession createUserSession(
            @Nullable RefreshToken refreshToken,
            @Nullable Instant expirationDate,
            User user,
            UserSessionRepositoryPort userSessionRepository,
            TimeProviderPort timeProvider,
            TokenProperties tokenProperties) {
            RefreshToken refreshTokenValue = requireNonNullElse(refreshToken, aRefreshToken());
            Instant expirationDateValue = requireNonNullElse(
                expirationDate,
                timeProvider.instant().plus(tokenProperties.refreshTokenValidityDuration())
            );
            UserSession userSession = new UserSession(refreshTokenValue, expirationDateValue, toUserPrincipal(user));
            userSessionRepository.create(userSession);
            return userSession;
        }
    }
}
