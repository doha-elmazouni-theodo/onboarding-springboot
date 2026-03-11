package com.theodo.springblueprint.features.users.domain.usecases;

import static com.theodo.springblueprint.features.users.domain.valueobjects.NewUserBuilder.aNewUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;
import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.exceptions.UsernameAlreadyExistsInRepositoryException;
import com.theodo.springblueprint.features.users.domain.usecases.signup.SignupCommand;
import com.theodo.springblueprint.features.users.domain.usecases.suts.SignupSut;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUser;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

@UnitTest
@RequiredArgsConstructor
class SignupUseCaseUnitTests {

    private final SignupSut sut;

    @Test
    void signup_create_a_new_user_in_repository() {
        SignupCommand signupCommand = createSignupCommand();

        // Act
        User user = sut.signup(signupCommand);

        ImmutableList<User> users = sut.infra().userRepository().findAll();
        assertThat(users).containsExactly(user);
    }

    @Test
    void signup_returns_the_created_user() {
        SignupCommand signupCommand = createSignupCommand();
        User expectedUser = getUser(signupCommand);

        // Act
        User user = sut.signup(signupCommand);

        assertThat(user).usingRecursiveComparison().ignoringFields("id").isEqualTo(expectedUser);
    }

    @Test
    void signup_twice_create_two_users_with_different_ids() {
        User user1 = sut.signup(createSignupCommand());

        // Act
        User user2 = sut.signup(createSignupCommand());

        assertThat(user2.id()).isNotEqualTo(user1.id());
    }

    @Test
    void signup_with_existing_username_throws() {
        SignupCommand signupCommand = createSignupCommand();
        User user = sut.signup(signupCommand);

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> sut.signup(signupCommand));

        exceptionAssertion
            .isInstanceOf(UsernameAlreadyExistsInRepositoryException.class)
            .hasMessage("Username '%s' already exist in repository".formatted(user.username().value()));
    }

    @Test
    void signup_create_usercredentials_in_repository() {
        SignupCommand signupCommand = createSignupCommand();

        // Act
        sut.signup(signupCommand);

        EncodedPassword encodedPassword = getPasswordFromRepository(signupCommand.username());
        boolean passwordMatches = sut.infra().passwordEncoder().matches(signupCommand.plainPassword(), encodedPassword);
        assertThat(passwordMatches).isTrue();
    }

    private EncodedPassword getPasswordFromRepository(Username username) {
        return sut.infra().userRepository().findUserCredentialByUsername(username).orElseThrow().encodedPassword();
    }

    private User getUser(SignupCommand signupCommand) {
        return new User(
            UUID.randomUUID(),
            signupCommand.name(),
            signupCommand.username(),
            sut.infra().timeProvider().instant(),
            signupCommand.roles()
        );
    }

    private static SignupCommand createSignupCommand() {
        NewUser inputUser = aNewUser().roles(Immutable.set.of(Role.ADMIN)).build();
        return new SignupCommand(inputUser.name(), inputUser.username(), "password", inputUser.roles());
    }
}
