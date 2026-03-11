package com.theodo.springblueprint.features.authentication.domain.exceptions;

import com.theodo.springblueprint.common.domain.valueobjects.Username;

public class UnknownUsernameException extends AbstractAuthenticationDomainException {

    public UnknownUsernameException(final Username username) {
        super("Username not found: %s".formatted(username.value()));
    }
}
