package com.theodo.springblueprint.features.authentication.api.endpoints.logout;

import static com.theodo.springblueprint.testhelpers.helpers.TokenHelpers.urlEncodeAccessToken;
import static com.theodo.springblueprint.testhelpers.helpers.TokenHelpers.urlEncodeRefreshToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.theodo.springblueprint.features.authentication.domain.usecases.suts.LogoutSut;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseWebMvcIntegrationTests;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = LogoutEndpoint.class)
@Import(LogoutSut.class)
class LogoutEndpointIntegrationTests extends BaseWebMvcIntegrationTests {

    private static final String LOGOUT_ENDPOINT = "/auth/logout";

    private final LogoutSut sut;

    protected LogoutEndpointIntegrationTests(LogoutSut sut, BaseWebMvcDependencies baseWebMvcDependencies) {
        super(baseWebMvcDependencies);
        this.sut = sut;
    }

    @Test
    void returns_expired_cookies() throws Exception {
        UserTokens userTokens = sut.infra().loginWithNewUser().tokens();

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(LOGOUT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", urlEncodeAccessToken(userTokens)))
                .cookie(new Cookie("refreshToken", urlEncodeRefreshToken(userTokens)))
        );

        resultActions
            .andExpect(status().isOk())
            .andExpect(cookie().maxAge("accessToken", 0))
            .andExpect(cookie().maxAge("refreshToken", 0))
            .andExpect(noContent());
    }

    @Test
    void returns_forbidden_when_not_authenticated() throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(post(LOGOUT_ENDPOINT).contentType(MediaType.APPLICATION_JSON));

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonIgnoreArrayOrder(
                    """
                    {"type":"about:blank","title":"errors.unauthorized","status":401,
                    "detail":"errors.unauthorized","instance":"/auth/logout"}"""
                )
            );
    }
}
