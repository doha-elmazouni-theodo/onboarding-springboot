package com.theodo.springblueprint.features.users.domain.ports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.common.infra.adapters.TimeProvider;
import com.theodo.springblueprint.features.authentication.domain.entities.UserCredential;
import com.theodo.springblueprint.features.authentication.domain.ports.UserCredentialsRepositoryPort;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.exceptions.UsernameAlreadyExistsInRepositoryException;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUser;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUserBuilder;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Transactional
@RequiredArgsConstructor
public abstract class UserRepositoryPortContractTests {

    private final UserRepositoryPort userRepository;
    private final UserCredentialsRepositoryPort userCredentialsRepositoryPort;

    @Nested
    class Create {

        @Test
        void creating_a_new_user_succeeds() {
            NewUser newUser = aNewUser().build();

            // Act
            userRepository.create(newUser);

            NewUser newUserFromDb = reconstructNewUserFromDb(newUser.username());
            assertThat(newUserFromDb).isEqualTo(newUser);
        }

        @Test
        void creating_a_new_user_returns_the_created_user() {
            NewUser newUser = aNewUser().build();

            // Act
            User user = userRepository.create(newUser);

            assertThat(userRepository.findAll()).containsExactly(user);
        }

        @Test
        void creating_a_new_user_with_an_existing_username_throws_UsernameAlreadyExistsInRepositoryException() {
            NewUser firstNewUser = aNewUser().username("username").build();
            NewUser secondNewUser = aNewUser().username("username").build();
            userRepository.create(firstNewUser);

            // Act
            var exceptionAssertion = assertThatThrownBy(() -> userRepository.create(secondNewUser));

            exceptionAssertion
                .isInstanceOf(UsernameAlreadyExistsInRepositoryException.class)
                .hasMessage("Username 'username' already exist in repository");
        }
    }

    private static NewUserBuilder aNewUser() {
        return NewUserBuilder.aNewUser().timeProvider(TimeProvider.UTC);
    }

    private NewUser reconstructNewUserFromDb(Username username) {
        User user = userRepository.findAll().detectOptional(u -> u.username().equals(username)).orElseThrow();
        UserCredential userCredential = userCredentialsRepositoryPort
            .findUserCredentialByUsername(username)
            .orElseThrow();
        return new NewUser(
            user.id(),
            user.name(),
            user.username(),
            userCredential.encodedPassword(),
            user.createdAt(),
            user.roles()
        );
    }
}
