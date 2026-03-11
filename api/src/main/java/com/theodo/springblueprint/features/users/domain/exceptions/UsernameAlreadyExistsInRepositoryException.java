package com.theodo.springblueprint.features.users.domain.exceptions;

import com.theodo.springblueprint.common.domain.exceptions.AbstractDomainException;
import com.theodo.springblueprint.common.domain.valueobjects.Username;

public class UsernameAlreadyExistsInRepositoryException extends AbstractDomainException {

    public UsernameAlreadyExistsInRepositoryException(Username username, Throwable cause) {
        super("Username '%s' already exist in repository".formatted(username.value()), cause);
    }
}
