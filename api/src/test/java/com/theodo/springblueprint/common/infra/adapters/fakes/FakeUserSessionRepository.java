package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.exceptions.CannotCreateUserSessionInRepositoryException;
import com.theodo.springblueprint.features.authentication.domain.exceptions.RefreshTokenExpiredOrNotFoundException;
import com.theodo.springblueprint.features.authentication.domain.ports.UserSessionRepositoryPort;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@RequiredArgsConstructor
@Import(FakeDatabase.class)
public class FakeUserSessionRepository implements UserSessionRepositoryPort {

    private final FakeDatabase fakeDb;

    @Override
    public void create(UserSession userSession) {
        ensureUserExists(userSession);
        ensureSessionDoesNotExist(userSession);
        fakeDb.sessionTable().put(userSession.refreshToken(), userSession);
    }

    @Override
    public Optional<UserSession> findByRefreshTokenAndExpirationDateAfter(RefreshToken refreshToken, Instant now) {
        return Optional.ofNullable(fakeDb.sessionTable().get(refreshToken)).filter(
            us -> us.expirationDate().isAfter(now) || us.expirationDate().equals(now)
        );
    }

    @Override
    public void deleteUserSessionByRefreshToken(RefreshToken refreshToken) {
        UserSession removedSession = fakeDb.sessionTable().remove(refreshToken);
        if (removedSession == null) {
            throw new RefreshTokenExpiredOrNotFoundException(refreshToken);
        }
    }

    @Override
    public void deleteUserSessionByExpirationDateBefore(Instant instant) {
        fakeDb
            .sessionTable()
            .values()
            .stream()
            .filter(us -> us.expirationDate().isBefore(instant))
            .map(UserSession::refreshToken)
            .toList() // necessary to avoid ConcurrentModificationException
            .forEach(fakeDb.sessionTable()::remove);
    }

    public ImmutableList<UserSession> findAll() {
        return fakeDb.sessionTable().toImmutableList();
    }

    private void ensureUserExists(UserSession userSession) {
        UUID userId = userSession.userPrincipal().id();
        if (!fakeDb.userTable().anySatisfy(u -> u.user().id().equals(userId))) {
            throw new CannotCreateUserSessionInRepositoryException(
                userSession.userPrincipal().username(),
                new DataIntegrityViolationException("User ID '%s' not found in repository".formatted(userId))
            );
        }
    }

    private void ensureSessionDoesNotExist(UserSession userSession) {
        if (fakeDb.sessionTable().containsKey(userSession.refreshToken())) {
            throw new CannotCreateUserSessionInRepositoryException(
                userSession.userPrincipal().username(),
                new DataIntegrityViolationException(
                    "RefreshToken '%s' already exist in repository".formatted(userSession.refreshToken().value())
                )
            );
        }
    }
}
