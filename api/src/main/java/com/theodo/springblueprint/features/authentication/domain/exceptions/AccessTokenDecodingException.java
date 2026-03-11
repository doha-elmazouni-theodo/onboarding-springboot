package com.theodo.springblueprint.features.authentication.domain.exceptions;

import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;

public class AccessTokenDecodingException extends AbstractAuthenticationDomainException {

    public AccessTokenDecodingException(String message, AccessToken token) {
        super("Cannot decode token '%s': %s".formatted(token.value(), message));
    }

    public AccessTokenDecodingException(Throwable cause, AccessToken token) {
        super("Cannot decode token '%s'".formatted(token.value()), cause);
    }
}
