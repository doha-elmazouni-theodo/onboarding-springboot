package com.theodo.springblueprint.features.authentication.domain.entities;

import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;

public record UserCredential(UserPrincipal userPrincipal, EncodedPassword encodedPassword) {
}
