package com.theodo.springblueprint.features.users.domain.ports;

import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUser;
import org.eclipse.collections.api.list.ImmutableList;

public interface UserRepositoryPort {
    User create(NewUser userToCreate);

    ImmutableList<User> findAll();
}
