package com.theodo.springblueprint.features.users.api.endpoints.signup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.theodo.springblueprint.features.users.domain.usecases.suts.SignupSut;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseWebMvcIntegrationTests;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = SignupEndpoint.class)
@Import(SignupSut.class)
class SignupEndpointIntegrationTests extends BaseWebMvcIntegrationTests {

    private static final String SIGNUP_ENDPOINT = "/auth/public/signup";

    private final SignupSut sut;

    protected SignupEndpointIntegrationTests(SignupSut sut, BaseWebMvcDependencies baseWebMvcDependencies) {
        super(baseWebMvcDependencies);
        this.sut = sut;
    }

    @Test
    void signing_up_returns_the_created_user_with_201_status() throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(
            post(SIGNUP_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name":"Name", "username":"Username",
                    "password":"password", "roles":["ADMIN"]}"""
                )
        );

        resultActions
            .andExpect(status().isCreated())
            .andExpect(
                jsonIgnoreArrayOrder(
                    """
                    {"id":"%s","name":"Name","username":"Username","roles":["ADMIN"]}""".formatted(
                        getUserId().toString()
                    )
                )
            );
    }

    private UUID getUserId() {
        return sut.infra().userRepository().findAll().get(0).id();
    }
}
