package com.theodo.springblueprint.features.authentication.domain.usecases.recreateusertokens;

import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;

public record RecreateUserTokensCommand(UserTokens previousUserToken) {
}
