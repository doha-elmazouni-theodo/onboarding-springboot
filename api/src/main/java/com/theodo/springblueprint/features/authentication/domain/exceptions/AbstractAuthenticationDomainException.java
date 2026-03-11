package com.theodo.springblueprint.features.authentication.domain.exceptions;

import com.theodo.springblueprint.common.domain.exceptions.AbstractDomainException;

public abstract class AbstractAuthenticationDomainException extends AbstractDomainException {

    public AbstractAuthenticationDomainException(String message) {
        super(message);
    }

    public AbstractAuthenticationDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
