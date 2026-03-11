package com.theodo.springblueprint.features.users.domain.usecases.suts;

import com.theodo.springblueprint.common.infra.adapters.fakes.FakeTimeProvider;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeUserRepository;
import com.theodo.springblueprint.features.authentication.domain.usecases.helpers.UserCreationHelpers;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.usecases.getusers.GetUsersUseCase;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.context.annotation.Import;

@Import({ GetUsersUseCase.class, GetUsersSut.Infra.class })
@RequiredArgsConstructor
public class GetUsersSut {

    private final GetUsersUseCase useCase;

    @Getter
    private final Infra infra;

    public ImmutableList<User> getUsers() {
        return useCase.handle();
    }

    @Import({ FakeUserRepository.class, TimeTestConfiguration.class })
    @RequiredArgsConstructor
    @Getter
    public static class Infra implements UserCreationHelpers {

        private final FakeUserRepository userRepository;
        private final FakeTimeProvider timeProvider;
    }
}
