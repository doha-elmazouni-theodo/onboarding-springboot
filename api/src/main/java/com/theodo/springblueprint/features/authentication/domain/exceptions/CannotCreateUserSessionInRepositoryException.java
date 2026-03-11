package com.theodo.springblueprint.features.authentication.domain.exceptions;

import com.theodo.springblueprint.common.domain.exceptions.AbstractDomainException;
import com.theodo.springblueprint.common.domain.valueobjects.Username;

public class CannotCreateUserSessionInRepositoryException extends AbstractDomainException {

    public CannotCreateUserSessionInRepositoryException(Username username, Throwable cause) {
        super("Cannot create UserSession in repository for user '%s'".formatted(username.value()), cause);
    }
}
