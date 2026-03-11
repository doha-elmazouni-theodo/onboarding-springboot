package com.theodo.springblueprint.features.authentication.domain.exceptions;

import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;

public class RefreshTokenExpiredOrNotFoundException extends AbstractAuthenticationDomainException {

    public RefreshTokenExpiredOrNotFoundException(RefreshToken refreshToken) {
        super("RefreshToken has expired or not present in database: %s".formatted(refreshToken.value()));
    }
}
