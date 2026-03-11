package com.theodo.springblueprint.features.authentication.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;

import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.usecases.suts.PurgeRefreshTokensSut;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import java.time.Duration;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

@UnitTest
@RequiredArgsConstructor
class PurgeRefreshTokensUseCaseUnitTests {

    private final PurgeRefreshTokensSut sut;

    @Test
    void purging_deletes_all_expired_refreshTokens() {
        User user = createUser();
        Instant start = sut.infra().timeProvider().instant();
        addSession(user, start);
        addSession(user, start.plusSeconds(1));
        UserSession nonExpiredUserSession = addSession(user, start.plusSeconds(3));

        sut.infra().timeProvider().moveTime(Duration.ofSeconds(2));

        // Act
        sut.purgeRefreshToken();

        ImmutableList<UserSession> userSessions = sut.infra().userSessionRepository().findAll();
        assertThat(userSessions).containsExactly(nonExpiredUserSession);
    }

    private User createUser() {
        return sut.infra().createUser().execute();
    }

    private UserSession addSession(User user, Instant expirationDate) {
        return sut.infra().createUserSession(user).expirationDate(expirationDate).execute();
    }
}
