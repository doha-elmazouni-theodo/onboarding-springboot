package com.theodo.springblueprint.features.authentication.domain.entities;

import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import java.time.Instant;

public record UserSession(RefreshToken refreshToken, Instant expirationDate, UserPrincipal userPrincipal) {
}
