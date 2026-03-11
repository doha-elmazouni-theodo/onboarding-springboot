package com.theodo.springblueprint.features.authentication.domain.usecases.login;

import com.theodo.springblueprint.common.domain.valueobjects.Username;

public record LoginCommand(Username username, String password) {
}
