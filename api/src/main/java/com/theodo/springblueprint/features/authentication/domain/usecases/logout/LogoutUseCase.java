package com.theodo.springblueprint.features.authentication.domain.usecases.logout;

import com.theodo.springblueprint.features.authentication.domain.ports.UserSessionRepositoryPort;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;

public class LogoutUseCase {

    private final UserSessionRepositoryPort userSessionRepository;

    public LogoutUseCase(UserSessionRepositoryPort userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    public void handle(final RefreshToken refreshToken) {
        userSessionRepository.deleteUserSessionByRefreshToken(refreshToken);
    }
}
