package com.theodo.springblueprint.features.authentication.domain.ports;

import com.theodo.springblueprint.features.authentication.domain.entities.TokenClaims;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;

public interface TokenClaimsCodecPort {
    AccessToken encode(TokenClaims tokenClaims);
    TokenClaims decodeWithoutExpirationValidation(AccessToken token);
}
