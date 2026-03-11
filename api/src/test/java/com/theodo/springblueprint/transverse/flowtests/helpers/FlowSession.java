package com.theodo.springblueprint.transverse.flowtests.helpers;

import com.theodo.springblueprint.testhelpers.baseclasses.AbstractApplicationTests;
import org.springframework.test.web.servlet.client.RestTestClient;

public record FlowSession(RestTestClient webTestClient)
    implements
        // Create an interface for each folder under "features" package
        AuthenticationApi,
        UsersApi {

    public FlowSession(AbstractApplicationTests baseApplicationTests) {
        this(baseApplicationTests.buildSessionRestTestClient());
    }
}
