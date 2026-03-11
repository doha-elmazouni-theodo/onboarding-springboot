package com.theodo.springblueprint.testhelpers.fixtures;

import static java.util.Objects.requireNonNullElse;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import jakarta.annotation.Nullable;
import java.util.UUID;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jilt.Builder;

public class UserFixture {

    private static final ImmutableSet<Role> DEFAULT_ROLES = Immutable.set.of();
    private static final TimeProviderPort DEFAULT_TIME_PROVIDER = TimeTestConfiguration.fakeTimeProvider();

    @Builder(factoryMethod = "aUser")
    public static User buildUser(
        @Nullable UUID id,
        @Nullable String name,
        @Nullable String username,
        @Nullable TimeProviderPort timeProvider,
        @Nullable ImmutableSet<Role> roles) {
        UUID idValue = requireNonNullElse(id, UUID.randomUUID());
        String nameValue = requireNonNullElse(name, "name");
        String usernameValue = requireNonNullElse(username, "username");
        TimeProviderPort timeProviderValue = requireNonNullElse(timeProvider, DEFAULT_TIME_PROVIDER);
        ImmutableSet<Role> rolesValue = requireNonNullElse(roles, DEFAULT_ROLES);

        return new User(idValue, nameValue, new Username(usernameValue), timeProviderValue.instant(), rolesValue);
    }
}
