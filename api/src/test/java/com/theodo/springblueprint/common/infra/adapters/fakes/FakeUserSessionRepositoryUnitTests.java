package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.ports.UserSessionRepositoryPortContractTests;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import org.eclipse.collections.api.list.ImmutableList;

@UnitTest
public class FakeUserSessionRepositoryUnitTests extends UserSessionRepositoryPortContractTests {
    private final FakeUserSessionRepository userSessionRepository;

    public FakeUserSessionRepositoryUnitTests(
        TimeTestConfiguration ignoredParamOnlyForContextLoading,
        FakeUserRepository userRepository,
        FakeUserSessionRepository userSessionRepository,
        FakeTimeProvider timeProvider) {
        super(userRepository, userSessionRepository, timeProvider);
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    protected ImmutableList<UserSession> getAllUserSessions() {
        return userSessionRepository.findAll();
    }
}
