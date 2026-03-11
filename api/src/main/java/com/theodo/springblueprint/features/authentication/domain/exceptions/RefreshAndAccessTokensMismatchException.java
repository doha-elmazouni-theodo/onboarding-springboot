package com.theodo.springblueprint.features.authentication.domain.exceptions;

import java.util.UUID;

public class RefreshAndAccessTokensMismatchException extends AbstractAuthenticationDomainException {

    public RefreshAndAccessTokensMismatchException(UUID userIdFromRefreshToken, UUID userIdFromAccessToken) {
        super(
            "User ids from RefreshToken and AccessToken don't match. RefreshToken userId: %s | AccessToken userId: %s"
                .formatted(
                    userIdFromRefreshToken,
                    userIdFromAccessToken
                )
        );
    }
}
