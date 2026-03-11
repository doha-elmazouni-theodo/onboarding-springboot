package com.theodo.springblueprint.features.authentication.domain.entities;

import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.domain.valueobjects.Username;
import java.util.UUID;
import org.eclipse.collections.api.set.ImmutableSet;

public record UserPrincipal(
    UUID id,
    Username username,

    ImmutableSet<Role> roles
) {
}
