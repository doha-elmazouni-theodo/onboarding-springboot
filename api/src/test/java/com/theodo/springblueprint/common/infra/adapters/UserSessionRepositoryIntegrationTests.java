package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.infra.database.entities.UserSessionDbEntity;
import com.theodo.springblueprint.common.infra.database.jparepositories.JpaUserSessionRepository;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.ports.UserSessionRepositoryPortContractTests;
import com.theodo.springblueprint.testhelpers.annotations.ParentNestedDataJpaTest;
import com.theodo.springblueprint.testhelpers.annotations.SetupDatabase;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import com.theodo.springblueprint.testhelpers.junitextensions.querycount.AssertQueryCount;
import com.theodo.springblueprint.testhelpers.junitextensions.querycount.Expected;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.context.annotation.Import;

@SetupDatabase
@Import({ UserSessionRepository.class, UserRepository.class, TimeTestConfiguration.class })
@ParentNestedDataJpaTest
@AssertQueryCount(
    sutClass = UserSessionRepository.class,
    value = {
        @Expected(
            count = 1,
            sutActMethod = "create",
            testMethod = "creating_a_usersession_for_a_non_existing_user_throws_CannotCreateUserSessionInRepositoryException"
        ),
        @Expected(
            count = 1,
            sutActMethod = "create",
            testMethod = "creating_a_usersession_for_an_existing_user_succeed"
        ),
        @Expected(
            count = 1,
            sutActMethod = "create",
            testMethod = "creating_a_usersession_with_an_existing_refreshtoken_throws_CannotCreateUserSessionInRepositoryException"
        ),
        @Expected(
            count = 3,
            sutActMethod = "deleteUserSessionByExpirationDateBefore",
            testMethod = "deleting_expired_sessions_keeps_valid_ones"
        ),
        @Expected(
            count = 3,
            sutActMethod = "deleteUserSessionByRefreshToken",
            testMethod = "deleting_userSession_with_existing_refreshToken_succeed"
        ),
        @Expected(
            count = 1,
            sutActMethod = "deleteUserSessionByRefreshToken",
            testMethod = "deleting_userSession_with_non_existing_refreshToken_throws_RefreshTokenExpiredOrNotFoundException"
        ),
        @Expected(
            count = 1,
            sutActMethod = "findByRefreshTokenAndExpirationDateAfter",
            testMethod = "finding_by_expiration_date_does_not_return_non_existing_session"
        ),
        @Expected(
            count = 1,
            sutActMethod = "findByRefreshTokenAndExpirationDateAfter",
            testMethod = "finding_by_refreshToken_does_not_return_expired_sessions"
        ),
        @Expected(
            count = 2,
            sutActMethod = "findByRefreshTokenAndExpirationDateAfter",
            testMethod = "finding_by_refreshToken_return_valid_session"
        ),
    }
)
public class UserSessionRepositoryIntegrationTests extends UserSessionRepositoryPortContractTests {

    private final JpaUserSessionRepository jpaUserSessionRepository;

    public UserSessionRepositoryIntegrationTests(
        UserRepository userRepository,
        UserSessionRepository userSessionRepository,
        TimeProviderPort timeProvider,
        JpaUserSessionRepository jpaUserSessionRepository) {
        super(userRepository, userSessionRepository, timeProvider);
        this.jpaUserSessionRepository = jpaUserSessionRepository;
    }

    @Override
    protected ImmutableList<UserSession> getAllUserSessions() {
        return Immutable.collectList(jpaUserSessionRepository.findAll(), UserSessionDbEntity::toUserSession);
    }
}
