package com.theodo.springblueprint.features.authentication.domain.properties;

import java.time.Duration;

public record TokenProperties(
    boolean https,
    Duration accessTokenValidityDuration,
    Duration refreshTokenValidityDuration
) {
}
