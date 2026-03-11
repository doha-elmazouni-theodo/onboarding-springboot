package com.theodo.springblueprint.features.authentication.domain.usecases.purgerefreshtokens;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.features.authentication.domain.ports.UserSessionRepositoryPort;

public class PurgeRefreshTokensUseCase {

    private final UserSessionRepositoryPort userSessionRepository;
    private final TimeProviderPort timeProvider;

    public PurgeRefreshTokensUseCase(UserSessionRepositoryPort userSessionRepository, TimeProviderPort timeProvider) {
        this.userSessionRepository = userSessionRepository;
        this.timeProvider = timeProvider;
    }

    public void handle() {
        userSessionRepository.deleteUserSessionByExpirationDateBefore(timeProvider.instant());
    }
}
