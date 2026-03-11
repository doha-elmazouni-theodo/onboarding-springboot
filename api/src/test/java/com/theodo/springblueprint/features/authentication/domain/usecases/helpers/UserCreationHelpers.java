package com.theodo.springblueprint.features.authentication.domain.usecases.helpers;

import static com.theodo.springblueprint.testhelpers.fixtures.NewUserFixture.buildNewUser;

import com.theodo.springblueprint.common.domain.ports.PasswordEncoderPort;
import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.entities.UserBuildExecutor;
import com.theodo.springblueprint.features.users.domain.entities.UserBuildExecutors;
import com.theodo.springblueprint.features.users.domain.ports.UserRepositoryPort;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUser;
import com.theodo.springblueprint.testhelpers.annotations.ExecutableBuilder;
import jakarta.annotation.Nullable;
import org.eclipse.collections.api.set.ImmutableSet;

public interface UserCreationHelpers {
    UserRepositoryPort userRepository();
    TimeProviderPort timeProvider();

    default UserBuildExecutors.Optionals createUser() {
        return UserBuildExecutor.start().timeProvider(timeProvider()).userRepository(userRepository());
    }

    class ExecutableBuilders {

        @ExecutableBuilder
        public static User createUser(
            @Nullable String name,
            @Nullable String username,
            @Nullable String plainPassword,
            @Nullable ImmutableSet<Role> roles,
            @Nullable PasswordEncoderPort passwordEncoder,
            TimeProviderPort timeProvider,
            UserRepositoryPort userRepository) {
            NewUser newUser = buildNewUser(name, username, plainPassword, roles, timeProvider, passwordEncoder);
            return userRepository.create(newUser);
        }
    }
}
