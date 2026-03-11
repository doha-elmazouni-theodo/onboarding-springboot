package com.theodo.springblueprint.features.users.domain.usecases.signup;

import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;
import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUser;
import java.time.Instant;
import java.util.UUID;

import org.eclipse.collections.api.set.ImmutableSet;

public record SignupCommand(String name, Username username, String plainPassword, ImmutableSet<Role> roles) {
    public NewUser toNewUser(UUID id, EncodedPassword encodedPassword, Instant creationDateTime) {
        return new NewUser(id, name(), username(), encodedPassword, creationDateTime, roles());
    }
}
