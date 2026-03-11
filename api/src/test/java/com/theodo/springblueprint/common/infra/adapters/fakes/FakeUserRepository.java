package com.theodo.springblueprint.common.infra.adapters.fakes;

import static com.theodo.springblueprint.testhelpers.helpers.Mapper.toUserPrincipal;

import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.features.authentication.domain.entities.UserCredential;
import com.theodo.springblueprint.features.authentication.domain.ports.UserCredentialsRepositoryPort;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.exceptions.UsernameAlreadyExistsInRepositoryException;
import com.theodo.springblueprint.features.users.domain.ports.UserRepositoryPort;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUser;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@RequiredArgsConstructor
@Import(FakeDatabase.class)
public class FakeUserRepository implements UserRepositoryPort, UserCredentialsRepositoryPort {

    private final FakeDatabase fakeDb;

    @Override
    public User create(NewUser userToCreate) {
        ensureUserDoesNotExist(userToCreate.username());
        User user = toUser(userToCreate);
        fakeDb
            .userTable()
            .put(user.username(), new FakeDatabase.UserWithPassword(user, userToCreate.encodedPassword()));
        return user;
    }

    @Override
    public ImmutableList<User> findAll() {
        return fakeDb.userTable().collect(FakeDatabase.UserWithPassword::user).toImmutableList();
    }

    @Override
    public Optional<UserCredential> findUserCredentialByUsername(Username username) {
        return Optional.ofNullable(fakeDb.userTable().get(username)).map(
            u -> new UserCredential(toUserPrincipal(u.user()), u.encodedPassword())
        );
    }

    private static User toUser(NewUser userToCreate) {
        return new User(
            userToCreate.id(),
            userToCreate.name(),
            userToCreate.username(),
            userToCreate.creationDateTime(),
            userToCreate.roles()
        );
    }

    private void ensureUserDoesNotExist(Username username) {
        if (fakeDb.userTable().containsKey(username)) {
            throw new UsernameAlreadyExistsInRepositoryException(username, new DataIntegrityViolationException(""));
        }
    }
}
