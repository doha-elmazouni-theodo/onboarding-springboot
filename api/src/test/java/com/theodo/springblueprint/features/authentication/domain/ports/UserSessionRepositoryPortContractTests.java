package com.theodo.springblueprint.features.authentication.domain.ports;

import static com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipalBuilder.aUserPrincipal;
import static com.theodo.springblueprint.testhelpers.fixtures.SimpleFixture.aRefreshToken;
import static com.theodo.springblueprint.testhelpers.helpers.Mapper.toUserPrincipal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.exceptions.CannotCreateUserSessionInRepositoryException;
import com.theodo.springblueprint.features.authentication.domain.exceptions.RefreshTokenExpiredOrNotFoundException;
import com.theodo.springblueprint.features.authentication.domain.usecases.helpers.UserCreationHelpers;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.ports.UserRepositoryPort;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Transactional
@RequiredArgsConstructor
@Getter
public abstract class UserSessionRepositoryPortContractTests implements UserCreationHelpers {

    private final UserRepositoryPort userRepository;
    private final UserSessionRepositoryPort userSessionRepository;
    private final TimeProviderPort timeProvider;

    @Nested
    class Create {
        @Test
        void creating_a_usersession_for_a_non_existing_user_throws_CannotCreateUserSessionInRepositoryException() {
            UserSession userSession = new UserSession(
                aRefreshToken(), timeProvider.instant(), aUserPrincipal().build()
            );

            // Act
            var exceptionAssertion = assertThatThrownBy(() -> userSessionRepository.create(userSession));

            exceptionAssertion
                .isInstanceOf(CannotCreateUserSessionInRepositoryException.class)
                .hasMessage("Cannot create UserSession in repository for user 'username'");
        }

        @Test
        void creating_a_usersession_for_an_existing_user_succeed() {
            User user = createUser().execute();
            UserSession userSession = newUserSession(user, timeProvider.instant());

            // Act
            userSessionRepository.create(userSession);

            assertThat(getAllUserSessions()).containsExactly(userSession);
        }

        @Test
        void creating_a_usersession_with_an_existing_refreshtoken_throws_CannotCreateUserSessionInRepositoryException() {
            User user = createUser().username("username").execute();
            UserSession userSession = newUserSession(user, timeProvider.instant());
            userSessionRepository.create(userSession);

            // Act
            var exceptionAssertion = assertThatThrownBy(() -> userSessionRepository.create(userSession));

            exceptionAssertion
                .isInstanceOf(CannotCreateUserSessionInRepositoryException.class)
                .hasMessage("Cannot create UserSession in repository for user 'username'");
        }
    }

    @Nested
    class DeleteUserSessionByRefreshToken {

        @Test
        void deleting_userSession_with_existing_refreshToken_succeed() {
            User user = createUser().execute();
            UserSession userSession = newUserSession(user, timeProvider.instant());
            userSessionRepository.create(userSession);

            // Act
            userSessionRepository.deleteUserSessionByRefreshToken(userSession.refreshToken());

            assertThat(getAllUserSessions()).isEmpty();
        }

        @Test
        void deleting_userSession_with_non_existing_refreshToken_throws_RefreshTokenExpiredOrNotFoundException() {
            RefreshToken randomRefreshToken = aRefreshToken();

            // Act
            var exceptionAssertion = assertThatThrownBy(
                () -> userSessionRepository.deleteUserSessionByRefreshToken(randomRefreshToken)
            );

            exceptionAssertion
                .isInstanceOf(RefreshTokenExpiredOrNotFoundException.class);
        }
    }

    @Nested
    class FindByRefreshTokenAndExpirationDateAfter {

        @Test
        void finding_by_refreshToken_does_not_return_expired_sessions() {
            User user = createUser().execute();
            UserSession userSession = newUserSession(user, timeProvider.instant());
            userSessionRepository.create(userSession);

            // Act
            Optional<UserSession> insertedUserSession = userSessionRepository.findByRefreshTokenAndExpirationDateAfter(
                userSession.refreshToken(),
                userSession.expirationDate().plusMillis(1)
            );

            assertThat(insertedUserSession).isEmpty();
        }

        @Test
        void finding_by_refreshToken_return_valid_session() {
            User user = createUser().execute();
            UserSession userSession = newUserSession(user, timeProvider.instant());
            userSessionRepository.create(userSession);

            // Act
            Optional<UserSession> insertedUserSessionOptional = userSessionRepository
                .findByRefreshTokenAndExpirationDateAfter(
                    userSession.refreshToken(),
                    userSession.expirationDate()
                );

            assertThat(insertedUserSessionOptional)
                .isPresent()
                .contains(userSession);
        }

        @Test
        void finding_by_expiration_date_does_not_return_non_existing_session() {
            // Act
            Optional<UserSession> insertedUserSession = userSessionRepository.findByRefreshTokenAndExpirationDateAfter(
                aRefreshToken(),
                timeProvider.instant()
            );

            assertThat(insertedUserSession).isEmpty();
        }
    }

    @Nested
    class DeleteUserSessionByExpirationDateBefore {

        @Test
        void deleting_expired_sessions_keeps_valid_ones() {
            User user = createUser().execute();
            Instant now = timeProvider.instant();
            UserSession expiredSession = newUserSession(user, now.minusMillis(1));
            UserSession firstValidSession = newUserSession(user, now);
            UserSession secondValidSession = newUserSession(user, now.plusMillis(1));
            userSessionRepository.create(expiredSession);
            userSessionRepository.create(firstValidSession);
            userSessionRepository.create(secondValidSession);

            // Act
            userSessionRepository.deleteUserSessionByExpirationDateBefore(now);

            ImmutableList<UserSession> allUserSessions = getAllUserSessions();
            assertThat(allUserSessions).containsExactlyInAnyOrder(firstValidSession, secondValidSession);
        }
    }

    private UserSession newUserSession(User user, Instant expirationDate) {
        return new UserSession(aRefreshToken(), expirationDate, toUserPrincipal(user));
    }

    // --------------------------------- Protected Methods ------------------------------- //
    protected abstract ImmutableList<UserSession> getAllUserSessions();
}
