package com.theodo.springblueprint.common.domain.exceptions;

public abstract class AbstractDomainException extends RuntimeException {

    public AbstractDomainException(String message) {
        super(message);
    }

    public AbstractDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
