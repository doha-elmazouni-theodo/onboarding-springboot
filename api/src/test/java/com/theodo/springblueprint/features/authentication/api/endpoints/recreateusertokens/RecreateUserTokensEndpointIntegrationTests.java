package com.theodo.springblueprint.features.authentication.api.endpoints.recreateusertokens;

import static com.theodo.springblueprint.testhelpers.helpers.TokenHelpers.urlEncodeAccessToken;
import static com.theodo.springblueprint.testhelpers.helpers.TokenHelpers.urlEncodeRefreshToken;
import static com.theodo.springblueprint.testhelpers.utils.StringUtils.urlEncode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.theodo.springblueprint.features.authentication.domain.usecases.suts.RecreateUserTokensSut;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseWebMvcIntegrationTests;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = RecreateUserTokensEndpoint.class)
@Import(RecreateUserTokensSut.class)
class RecreateUserTokensEndpointIntegrationTests extends BaseWebMvcIntegrationTests {

    private static final String RECREATE_USER_TOKENS_ENDPOINT = "/auth/public/refreshToken";

    private final RecreateUserTokensSut sut;

    protected RecreateUserTokensEndpointIntegrationTests(RecreateUserTokensSut sut,
        BaseWebMvcDependencies baseWebMvcDependencies) {
        super(baseWebMvcDependencies);
        this.sut = sut;
    }

    @Test
    void returns_new_AccessToken_and_RefreshToken_in_cookies() throws Exception {
        UserTokens userTokens = sut.infra().loginWithNewUser().tokens();

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(RECREATE_USER_TOKENS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", urlEncodeAccessToken(userTokens)))
                .cookie(new Cookie("refreshToken", urlEncodeRefreshToken(userTokens)))
        );

        resultActions
            .andExpect(status().isOk())
            .andExpect(cookie().value("accessToken", urlEncodeLastCreatedToken()))
            .andExpect(cookie().value("refreshToken", urlEncode("cfcd2084-95d5-35ef-a6e7-dff9f98764da")))
            .andExpect(noContent());
    }

    private String urlEncodeLastCreatedToken() {
        AccessToken accessToken = sut.infra().tokenClaimsCodec().lastCreatedToken().orElseThrow();
        return urlEncode(accessToken.value());
    }
}
