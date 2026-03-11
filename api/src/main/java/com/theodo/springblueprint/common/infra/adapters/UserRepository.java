package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.common.infra.database.entities.UserDbEntity;
import com.theodo.springblueprint.common.infra.database.jparepositories.JpaUserRepository;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.features.authentication.domain.entities.UserCredential;
import com.theodo.springblueprint.features.authentication.domain.ports.UserCredentialsRepositoryPort;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.exceptions.UsernameAlreadyExistsInRepositoryException;
import com.theodo.springblueprint.features.users.domain.ports.UserRepositoryPort;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUser;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRepository implements UserRepositoryPort, UserCredentialsRepositoryPort {

    private final JpaUserRepository jpaUserRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public User create(NewUser userToCreate) {
        UserDbEntity userDbEntity = UserDbEntity.from(userToCreate);
        try {
            entityManager.persist(userDbEntity);
            entityManager.flush();
        } catch (ConstraintViolationException ex) {
            throw new UsernameAlreadyExistsInRepositoryException(userToCreate.username(), ex);
        }
        return userDbEntity.toUser();
    }

    @Override
    public ImmutableList<User> findAll() {
        return Immutable.collectList(jpaUserRepository.findAll(), UserDbEntity::toUser);
    }

    @Override
    // TODO: Retrieve from the database only the fields needed for UserCredential
    public Optional<UserCredential> findUserCredentialByUsername(Username username) {
        return jpaUserRepository.findByUsername(username.value()).map(UserDbEntity::toUserCredential);
    }
}
