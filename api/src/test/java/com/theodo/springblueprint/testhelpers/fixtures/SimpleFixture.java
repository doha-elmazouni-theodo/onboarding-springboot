package com.theodo.springblueprint.testhelpers.fixtures;

import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.features.authentication.domain.usecases.login.LoginCommand;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import java.util.UUID;

public interface SimpleFixture {
    static UserTokens aUserTokens(AccessToken accessToken) {
        return new UserTokens(accessToken, aRefreshToken());
    }

    static RefreshToken aRefreshToken() {
        return new RefreshToken(UUID.randomUUID().toString());
    }

    static LoginCommand aLoginCommand() {
        return aLoginCommand("password");
    }

    static LoginCommand aLoginCommand(String password) {
        return new LoginCommand(new Username("username"), password);
    }
}
