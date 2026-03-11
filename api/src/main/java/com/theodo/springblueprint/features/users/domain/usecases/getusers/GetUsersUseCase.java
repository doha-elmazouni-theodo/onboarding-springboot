package com.theodo.springblueprint.features.users.domain.usecases.getusers;

import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.ports.UserRepositoryPort;
import org.eclipse.collections.api.list.ImmutableList;

public class GetUsersUseCase {

    private final UserRepositoryPort userRepository;

    public GetUsersUseCase(final UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public ImmutableList<User> handle() {
        return userRepository.findAll();
    }
}
