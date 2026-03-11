package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.features.users.domain.ports.UserRepositoryPortContractTests;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;

@UnitTest
public class FakeUserRepositoryUnitTests extends UserRepositoryPortContractTests {

    public FakeUserRepositoryUnitTests(FakeUserRepository userRepository) {
        super(userRepository, userRepository);
    }
}
