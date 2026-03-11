package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.features.authentication.domain.ports.UserCredentialsRepositoryPort;
import com.theodo.springblueprint.features.users.domain.ports.UserRepositoryPort;
import com.theodo.springblueprint.features.users.domain.ports.UserRepositoryPortContractTests;
import com.theodo.springblueprint.testhelpers.annotations.ParentNestedDataJpaTest;
import com.theodo.springblueprint.testhelpers.annotations.SetupDatabase;
import com.theodo.springblueprint.testhelpers.junitextensions.querycount.AssertQueryCount;
import com.theodo.springblueprint.testhelpers.junitextensions.querycount.Expected;
import org.springframework.context.annotation.Import;

@SetupDatabase
@Import({ UserRepository.class })
@ParentNestedDataJpaTest
@AssertQueryCount(
    sutClass = UserRepository.class,
    value = {
        @Expected(count = 1, sutActMethod = "create", testMethod = "creating_a_new_user_succeeds"),
        @Expected(count = 1, sutActMethod = "create", testMethod = "creating_a_new_user_returns_the_created_user"),
        @Expected(
            count = 1,
            sutActMethod = "create",
            testMethod = "creating_a_new_user_with_an_existing_username_throws_UsernameAlreadyExistsInRepositoryException"
        ),
    }
)
public class UserRepositoryIntegrationTests extends UserRepositoryPortContractTests {
    public UserRepositoryIntegrationTests(
        UserRepositoryPort userRepository,
        UserCredentialsRepositoryPort userCredentialsRepositoryPort) {
        super(userRepository, userCredentialsRepositoryPort);
    }
}
