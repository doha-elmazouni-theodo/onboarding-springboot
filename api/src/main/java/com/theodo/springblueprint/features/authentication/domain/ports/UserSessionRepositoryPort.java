package com.theodo.springblueprint.features.authentication.domain.ports;

import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import java.time.Instant;
import java.util.Optional;

public interface UserSessionRepositoryPort {
    void create(UserSession userSession);

    Optional<UserSession> findByRefreshTokenAndExpirationDateAfter(RefreshToken refreshToken, Instant now);

    void deleteUserSessionByRefreshToken(RefreshToken refreshToken);

    void deleteUserSessionByExpirationDateBefore(Instant instant);
}
