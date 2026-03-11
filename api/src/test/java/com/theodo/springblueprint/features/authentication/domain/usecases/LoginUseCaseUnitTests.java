package com.theodo.springblueprint.features.authentication.domain.usecases;

import static com.theodo.springblueprint.testhelpers.fixtures.SimpleFixture.aLoginCommand;
import static com.theodo.springblueprint.testhelpers.helpers.Mapper.toUserPrincipal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.features.authentication.domain.entities.TokenClaims;
import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;
import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.events.UserLoggedInEvent;
import com.theodo.springblueprint.features.authentication.domain.exceptions.BadUserCredentialException;
import com.theodo.springblueprint.features.authentication.domain.exceptions.UnknownUsernameException;
import com.theodo.springblueprint.features.authentication.domain.usecases.login.LoginCommand;
import com.theodo.springblueprint.features.authentication.domain.usecases.login.LoginUseCase;
import com.theodo.springblueprint.features.authentication.domain.usecases.suts.LoginSut;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.entities.UserBuildExecutors;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import java.time.Instant;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

@UnitTest
@RequiredArgsConstructor
class LoginUseCaseUnitTests {
    private final LoginSut sut;

    @Test
    void login_with_good_username_and_password_returns_user_tokens() {
        LoginCommand loginCommand = aLoginCommand();
        User user = createUser(loginCommand).execute();

        UserPrincipal expectedUserPrincipal = toUserPrincipal(user);
        Instant expectedExpirationTime = sut
            .infra()
            .timeProvider()
            .instant()
            .plus(sut.infra().tokenProperties().accessTokenValidityDuration());

        // Act
        UserTokens tokens = sut.login(loginCommand);

        TokenClaims claims = getTokenClaims(tokens);
        assertThat(claims)
            .returns(expectedUserPrincipal, TokenClaims::userPrincipal)
            .returns(expectedExpirationTime, TokenClaims::expirationTime);
    }

    @Test
    void login_with_good_username_and_password_returns_new_tokens_each_time() {
        LoginCommand loginCommand = aLoginCommand();
        createUser(loginCommand).execute();
        UserTokens firstTokens = sut.login(loginCommand);

        // Act
        UserTokens secondTokens = sut.login(loginCommand);

        assertThat(secondTokens)
            .doesNotReturn(firstTokens.accessToken(), UserTokens::accessToken)
            .doesNotReturn(firstTokens.refreshToken(), UserTokens::refreshToken);
    }

    @Test
    void login_with_good_username_and_password_publishes_UserLoggedInEvent() {
        LoginCommand loginCommand = aLoginCommand();
        User user = createUser(loginCommand).execute();

        UserPrincipal expectedUserPrincipal = toUserPrincipal(user);

        // Act
        sut.login(loginCommand);

        Optional<UserLoggedInEvent> lastUserLoggedInEvent = sut.infra()
            .eventPublisher()
            .lastEvent(UserLoggedInEvent.class);
        assertThat(lastUserLoggedInEvent)
            .isPresent()
            .get()
            .returns(LoginUseCase.class, UserLoggedInEvent::sourceType)
            .returns(expectedUserPrincipal, UserLoggedInEvent::userPrincipal)
            .returns("Login attempt successful for user with id: " + user.id(), UserLoggedInEvent::toString);
    }

    @Test
    void login_with_good_username_and_password_returns_a_refreshToken_with_expected_duration() {
        LoginCommand loginCommand = aLoginCommand();
        User user = createUser(loginCommand).execute();

        UserSession expectedUserSession = new UserSession(
            new RefreshToken("cfcd2084-95d5-35ef-a6e7-dff9f98764da"),
            sut.infra().timeProvider().instant().plus(sut.infra().tokenProperties().refreshTokenValidityDuration()),
            toUserPrincipal(user)
        );

        // Act
        sut.login(loginCommand);

        ImmutableList<UserSession> userSessions = sut.infra().userSessionRepository().findAll();
        assertThat(userSessions).containsExactly(expectedUserSession);
    }

    @Test
    void login_with_non_existing_username_throws_UnknownUsernameException() {
        LoginCommand loginCommand = aLoginCommand();

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> sut.login(loginCommand));

        exceptionAssertion
            .isInstanceOf(UnknownUsernameException.class)
            .hasMessage("Username not found: username");
    }

    @Test
    void login_with_good_username_and_bad_password_throws_BadUserCredentialException() {
        LoginCommand loginCommand = aLoginCommand("bad_password");
        createUser(loginCommand).plainPassword("password").execute();

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> sut.login(loginCommand));

        exceptionAssertion
            .isInstanceOf(BadUserCredentialException.class)
            .hasMessage("Bad password provided for username: username");
    }

    private TokenClaims getTokenClaims(UserTokens tokens) {
        return sut.infra().tokenClaimsCodec().decodeWithoutExpirationValidation(tokens.accessToken());
    }

    private UserBuildExecutors.Optionals createUser(LoginCommand loginCommand) {
        return sut
            .infra()
            .createUser()
            .username(loginCommand.username().value())
            .plainPassword(loginCommand.password());
    }

}
