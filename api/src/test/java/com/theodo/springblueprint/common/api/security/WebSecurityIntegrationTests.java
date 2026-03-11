package com.theodo.springblueprint.common.api.security;

import static com.theodo.springblueprint.common.api.security.SecurityFakeEndpoint.ADMIN_ONLY_POST_ENDPOINT;
import static com.theodo.springblueprint.common.api.security.SecurityFakeEndpoint.PERMIT_ALL_POST_ENDPOINT;
import static com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipalBuilder.aUserPrincipal;
import static com.theodo.springblueprint.testhelpers.utils.StringUtils.urlEncode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.features.authentication.domain.entities.TokenClaims;
import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;
import com.theodo.springblueprint.features.authentication.domain.ports.TokenClaimsCodecPort;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseWebMvcIntegrationTests;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest({ SecurityFakeEndpoint.class })
// TODO: this was added because AuthenticationProblemEntryPoint was stale. Try to remove this.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WebSecurityIntegrationTests extends BaseWebMvcIntegrationTests {

    private final TokenClaimsCodecPort tokenClaimsCodec;
    private final TimeProviderPort timeProvider;

    protected WebSecurityIntegrationTests(
        TokenClaimsCodecPort tokenClaimsCodec,
        TimeProviderPort timeProvider,
        BaseWebMvcDependencies baseWebMvcDependencies) {
        super(baseWebMvcDependencies);
        this.timeProvider = timeProvider;
        this.tokenClaimsCodec = tokenClaimsCodec;
    }

    @Test
    void post_on_permit_all_endpoint_with_expired_token_returns_200() throws Exception {
        AccessToken expiredAccessToken = getExpiredAccessToken(getUserPrincipal(Role.USER));

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(PERMIT_ALL_POST_ENDPOINT).cookie(new Cookie("accessToken", urlEncode(expiredAccessToken.value())))
        );

        resultActions.andExpect(status().isOk()).andExpect(content().string("permitAll"));
    }

    @Test
    void post_on_admin_only_endpoint_with_admin_token_returns_200() throws Exception {
        UserPrincipal adminPrincipal = getUserPrincipal(Role.ADMIN);

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(ADMIN_ONLY_POST_ENDPOINT).cookie(getAccessTokenCookie(adminPrincipal))
        );

        resultActions.andExpect(status().isOk()).andExpect(content().string("adminOnly"));
    }

    @Test
    void post_on_admin_only_endpoint_with_non_admin_token_returns_403() throws Exception {
        UserPrincipal userPrincipal = getUserPrincipal(Role.USER);

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(ADMIN_ONLY_POST_ENDPOINT).cookie(getAccessTokenCookie(userPrincipal))
        );

        resultActions
            .andExpect(status().isForbidden())
            .andExpect(
                jsonStrictArrayOrder(
                    """
                    {"type":"about:blank","title":"errors.access_denied",
                    "status":403,"detail":"errors.access_denied",
                    "instance":"/security/admin/get"}"""
                )
            );
    }

    @Test
    void post_on_admin_only_endpoint_without_token_returns_401() throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(post(ADMIN_ONLY_POST_ENDPOINT));

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonStrictArrayOrder(
                    """
                    {"type":"about:blank","title":"errors.unauthorized","status":401,
                    "detail":"errors.unauthorized","instance":"/security/admin/get"}"""
                )
            );
    }

    @Test
    void post_on_admin_only_endpoint_with_expired_token_returns_401() throws Exception {
        AccessToken expiredAccessToken = getExpiredAccessToken(getUserPrincipal(Role.ADMIN));

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(ADMIN_ONLY_POST_ENDPOINT).cookie(new Cookie("accessToken", urlEncode(expiredAccessToken.value())))
        );

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonStrictArrayOrder(
                    """
                    {"type":"about:blank","title":"errors.unauthorized","status":401,
                    "detail":"errors.unauthorized","instance":"/security/admin/get"}"""
                )
            );
    }

    @Test
    void post_on_admin_only_endpoint_with_malformed_token_returns_401() throws Exception {
        AccessToken malformedToken = new AccessToken("123");

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(ADMIN_ONLY_POST_ENDPOINT).cookie(new Cookie("accessToken", urlEncode(malformedToken.value())))
        );

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonStrictArrayOrder(
                    """
                    {"type":"about:blank","title":"errors.unauthorized","status":401,
                    "detail":"errors.unauthorized","instance":"/security/admin/get"}"""
                )
            );
    }

    @Test
    void preflight_request_with_allowed_origin_returns_200() throws Exception {
        String localDevFrontendUrl = "http://localhost:3000";

        // Act
        ResultActions resultActions = mockMvc.perform(
            options(PERMIT_ALL_POST_ENDPOINT)
                // Value of 'Origin' header should be the same as the one specified in the 'addAllowedOrigin' method
                // of the CorsConfiguration class. Or, if you are using the 'addAllowedOriginPattern' method, the value
                // of 'Origin' header should match the pattern specified in the 'addAllowedOriginPattern' method of
                // the CorsConfiguration class
                .header("Origin", localDevFrontendUrl)
                .header("Access-Control-Request-Method", "POST")
        );

        resultActions.andExpect(status().isOk()).andExpect(noContent());
    }

    private static UserPrincipal getUserPrincipal(Role role) {
        return aUserPrincipal().roles(Immutable.set.of(role)).build();
    }

    private AccessToken getExpiredAccessToken(UserPrincipal userPrincipal) {
        Instant now = timeProvider.instant();
        return tokenClaimsCodec.encode(new TokenClaims(userPrincipal, now.minusSeconds(20), now.minusSeconds(10)));
    }
}
