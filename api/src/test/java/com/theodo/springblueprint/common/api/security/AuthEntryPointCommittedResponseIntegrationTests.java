package com.theodo.springblueprint.common.api.security;

import static com.theodo.springblueprint.common.api.security.SecurityFakeEndpoint.ADMIN_ONLY_POST_ENDPOINT;
import static com.theodo.springblueprint.testhelpers.utils.StringUtils.urlEncode;

import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseApplicationTestsWithoutDb;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Import({ SecurityFakeEndpoint.class })
class AuthEntryPointCommittedResponseIntegrationTests extends BaseApplicationTestsWithoutDb {

    private static final String committedResponseBody = """
        {
          "status": "ok",
          "message": "Request intercepted by filter"
        }
        """;

    @Order(Ordered.HIGHEST_PRECEDENCE)
    private static final class PreCommitResponseFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.setContentLength(committedResponseBody.getBytes(StandardCharsets.UTF_8).length);
            try (PrintWriter out = response.getWriter()) {
                out.write(committedResponseBody);
                out.flush(); // commit the response
            }

            chain.doFilter(request, res);
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Primary
        @Bean
        public Filter preCommitResponseFilter() {
            return new PreCommitResponseFilter();
        }
    }

    @Test
    void returns_committed_response_when_already_committed_before_auth_entry_point() {
        AccessToken malformedToken = new AccessToken("123");

        // Act
        var response = buildSessionRestTestClient()
            .post()
            .uri(ADMIN_ONLY_POST_ENDPOINT)
            .cookie("accessToken", urlEncode(malformedToken.value()))
            .exchange();

        response
            .expectStatus()
            .isOk()
            .expectBody()
            .json(committedResponseBody);
    }
}
