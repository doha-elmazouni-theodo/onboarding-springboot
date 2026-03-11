package com.theodo.springblueprint.transverse.flowtests;

import com.theodo.springblueprint.testhelpers.baseclasses.BaseApplicationTestsWithDb;
import com.theodo.springblueprint.transverse.flowtests.helpers.FlowSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationFlowApplicationTests extends BaseApplicationTestsWithDb {

    @Test
    void signup_login_getUsers_refreshToken() {
        FlowSession session = new FlowSession(this);

        session
            .getUsersShouldReturnStatus(HttpStatus.UNAUTHORIZED)
            .signupAsAdmin("admin1", "password1")
            .login("admin1", "password1")
            .getUsers(
                users -> assertThat(users).containsExactlyInAnyOrder("admin1")
            )
            .refreshToken()
            .logout()
            .getUsersShouldReturnStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void example_with_two_sessions() {
        FlowSession userSession = new FlowSession(this);
        userSession.signupAsUser("user1", "password1");

        FlowSession adminSession = new FlowSession(this);

        adminSession
            .signupAsAdmin("admin1", "password1")
            .login("admin1", "password1")
            .getUsers(
                users -> assertThat(users).containsExactlyInAnyOrder("user1", "admin1")
            );

        userSession
            .login("user1", "password1")
            .getUsersShouldReturnStatus(HttpStatus.FORBIDDEN)
            .logout();
    }
}
