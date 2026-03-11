package com.theodo.springblueprint.features.authentication.domain.entities;

import java.time.Instant;

public record TokenClaims(UserPrincipal userPrincipal, Instant creationTime, Instant expirationTime) {
}
