package com.theodo.springblueprint.features.users.domain.entities;

import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.domain.valueobjects.Username;
import java.time.Instant;
import java.util.UUID;
import org.eclipse.collections.api.set.ImmutableSet;

public record User(UUID id, String name, Username username, Instant createdAt, ImmutableSet<Role> roles) {
}
