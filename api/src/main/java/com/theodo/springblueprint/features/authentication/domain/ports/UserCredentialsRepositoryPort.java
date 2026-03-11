package com.theodo.springblueprint.features.authentication.domain.ports;

import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.features.authentication.domain.entities.UserCredential;
import java.util.Optional;

public interface UserCredentialsRepositoryPort {
    Optional<UserCredential> findUserCredentialByUsername(Username username);
}
