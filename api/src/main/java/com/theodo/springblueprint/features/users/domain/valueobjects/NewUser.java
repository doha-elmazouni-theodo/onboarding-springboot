package com.theodo.springblueprint.features.users.domain.valueobjects;

import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;
import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.domain.valueobjects.Username;
import java.time.Instant;
import java.util.UUID;

import org.eclipse.collections.api.set.ImmutableSet;

public record NewUser(
    UUID id,
    String name,
    Username username,
    EncodedPassword encodedPassword,
    Instant creationDateTime,
    ImmutableSet<Role> roles
) {
}
