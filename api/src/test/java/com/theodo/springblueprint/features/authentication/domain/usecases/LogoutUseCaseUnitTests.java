package com.theodo.springblueprint.features.authentication.domain.usecases;

import static com.theodo.springblueprint.testhelpers.fixtures.SimpleFixture.aRefreshToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.exceptions.RefreshTokenExpiredOrNotFoundException;
import com.theodo.springblueprint.features.authentication.domain.usecases.suts.LogoutSut;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;

import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

@UnitTest
@RequiredArgsConstructor
class LogoutUseCaseUnitTests {

    private final LogoutSut sut;

    @Test
    void logout_with_existing_refreshToken_deletes_session_from_repository() {
        UserSession userSession1 = sut.infra().loginWithNewUser().session();
        UserSession userSession2 = sut.infra().loginWithNewUser().session();

        // Act
        sut.logout(userSession1.refreshToken());

        ImmutableList<UserSession> userSessions = sut.infra().userSessionRepository().findAll();
        assertThat(userSessions).containsExactly(userSession2);
    }

    @Test
    void logout_with_non_existing_refreshToken_throws_RefreshTokenExpiredOrNotFoundException() {
        RefreshToken nonExistingRefreshToken = aRefreshToken();

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> sut.logout(nonExistingRefreshToken));

        exceptionAssertion
            .isInstanceOf(RefreshTokenExpiredOrNotFoundException.class)
            .hasMessage(
                "RefreshToken has expired or not present in database: " + nonExistingRefreshToken.value()
            );
    }
}
