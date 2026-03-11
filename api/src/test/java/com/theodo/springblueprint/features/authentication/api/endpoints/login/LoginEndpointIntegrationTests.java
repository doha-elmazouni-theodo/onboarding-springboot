package com.theodo.springblueprint.features.authentication.api.endpoints.login;

import static com.theodo.springblueprint.testhelpers.utils.StringUtils.urlEncode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.theodo.springblueprint.features.authentication.domain.usecases.suts.LoginSut;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseWebMvcIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = LoginEndpoint.class)
@Import(LoginSut.class)
class LoginEndpointIntegrationTests extends BaseWebMvcIntegrationTests {

    private static final String LOGIN_ENDPOINT = "/auth/public/login";

    private final LoginSut sut;

    protected LoginEndpointIntegrationTests(LoginSut sut, BaseWebMvcDependencies baseWebMvcDependencies) {
        super(baseWebMvcDependencies);
        this.sut = sut;
    }

    @Test
    void returns_AccessToken_and_RefreshToken_in_cookies() throws Exception {
        sut.infra().createUser().username("usernameCreated").plainPassword("passwordCreated").execute();

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"usernameCreated", "password":"passwordCreated"}"""
                )
        );

        resultActions
            .andExpect(status().isOk())
            .andExpect(cookie().value("accessToken", urlEncodeLastCreatedToken()))
            .andExpect(cookie().value("refreshToken", urlEncode("cfcd2084-95d5-35ef-a6e7-dff9f98764da")))
            .andExpect(noContent());
    }

    @Test
    void returns_401_if_user_does_not_exist() throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(
            post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"non_existing_user", "password":"any_password"}"""
                )
        );

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonIgnoreArrayOrder(
                    """
                    {"type":"about:blank","title":"errors.unauthorized","status":401,
                    "detail":"errors.unauthorized","instance":"/auth/public/login"}"""
                )
            );
    }

    private String urlEncodeLastCreatedToken() {
        AccessToken accessToken = sut.infra().tokenClaimsCodec().lastCreatedToken().orElseThrow();
        return urlEncode(accessToken.value());
    }
}
