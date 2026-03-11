package com.theodo.springblueprint.features.authentication.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.features.authentication.domain.exceptions.RefreshAndAccessTokensMismatchException;
import com.theodo.springblueprint.features.authentication.domain.exceptions.RefreshTokenExpiredOrNotFoundException;
import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.usecases.helpers.LoginHelpers;
import com.theodo.springblueprint.features.authentication.domain.usecases.recreateusertokens.RecreateUserTokensCommand;
import com.theodo.springblueprint.features.authentication.domain.usecases.suts.RecreateUserTokensSut;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

@UnitTest
@RequiredArgsConstructor
class RecreateUserTokensUseCaseUnitTests {

    private final RecreateUserTokensSut sut;

    @Test
    void recreating_tokens_returns_new_user_tokens_and_update_user_sessions() {
        UserTokens oldUserTokens = sut.infra().loginWithNewUser().tokens();

        // Act
        UserTokens newUserTokens = sut.recreateUserTokens(new RecreateUserTokensCommand(oldUserTokens));

        AccessToken lastCreatedAccessToken = sut.infra().tokenClaimsCodec().lastCreatedToken().orElseThrow();
        ImmutableList<UserSession> sessions = sut.infra().userSessionRepository().findAll();

        assertThat(oldUserTokens)
            .doesNotReturn(newUserTokens.accessToken(), UserTokens::accessToken)
            .doesNotReturn(newUserTokens.refreshToken(), UserTokens::refreshToken);
        assertThat(newUserTokens).returns(lastCreatedAccessToken, UserTokens::accessToken);
        assertThat(sessions)
            .singleElement()
            .returns(newUserTokens.refreshToken(), UserSession::refreshToken);
    }

    @Test
    void recreating_tokens_with_non_existing_refreshToken_throws_RefreshTokenExpiredOrNotFoundException() {
        UserTokens userTokens = createUserTokens();

        // Act
        var exceptionAssertion = assertThatThrownBy(
            () -> sut.recreateUserTokens(new RecreateUserTokensCommand(userTokens))
        );

        exceptionAssertion
            .isInstanceOf(RefreshTokenExpiredOrNotFoundException.class)
            .hasMessage(
                "RefreshToken has expired or not present in database: " + userTokens.refreshToken().value()
            );
    }

    @Test
    void recreating_tokens_with_accessToken_and_refreshToken_belonging_to_different_users_throws_RefreshAndAccessTokensMismatchException() {
        LoginHelpers.LoginData user1LoginData = sut.infra().loginWithNewUser();
        LoginHelpers.LoginData user2LoginData = sut.infra().loginWithNewUser();
        UserTokens mismatchedTokens = new UserTokens(
            user1LoginData.tokens().accessToken(),
            user2LoginData.tokens().refreshToken()
        );

        // Act
        var exceptionAssertion = assertThatThrownBy(
            () -> sut.recreateUserTokens(new RecreateUserTokensCommand(mismatchedTokens))
        );

        exceptionAssertion
            .isInstanceOf(RefreshAndAccessTokensMismatchException.class)
            .hasMessage(
                "User ids from RefreshToken and AccessToken don't match. RefreshToken userId: %s | AccessToken userId: %s"
                    .formatted(
                        user2LoginData.user().id(),
                        user1LoginData.user().id()
                    )
            );
    }

    private UserTokens createUserTokens() {
        User user = sut.infra().createUser().execute();
        return sut.infra().newUserTokens(user);
    }
}
