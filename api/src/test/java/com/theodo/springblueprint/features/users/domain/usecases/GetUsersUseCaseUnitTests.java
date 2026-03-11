package com.theodo.springblueprint.features.users.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;

import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.usecases.suts.GetUsersSut;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

@UnitTest
@RequiredArgsConstructor
class GetUsersUseCaseUnitTests {
    private final GetUsersSut sut;

    @Test
    void returns_existing_users_in_repository() {
        User user1 = sut.infra().createUser().execute();
        User user2 = sut.infra().createUser().execute();

        // Act
        ImmutableList<User> usersInRepository = sut.getUsers();

        assertThat(usersInRepository).containsExactlyInAnyOrder(user1, user2);
    }
}
