package com.theodo.springblueprint.common.api.exceptionhandling;

import static com.theodo.springblueprint.testhelpers.baseclasses.exceptionhandling.ExceptionHandlingFakeEndpoint.EXCEPTION_PERMIT_ALL_GET_ENDPOINT;
import static com.theodo.springblueprint.testhelpers.utils.JsonUtils.jsonIgnoreArrayOrder;
import static org.assertj.core.api.Assertions.assertThat;

import com.theodo.springblueprint.common.api.events.UnhandledExceptionEvent;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseApplicationTestsWithoutDb;
import com.theodo.springblueprint.testhelpers.baseclasses.exceptionhandling.ExceptionHandlingFakeEndpoint;
import com.theodo.springblueprint.testhelpers.helpers.InMemoryEventListener;
import org.assertj.core.api.ThrowingConsumer;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.client.RestTestClient;

@Import({ ExceptionHandlingFakeEndpoint.class, InMemoryEventListener.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GlobalExceptionControllerApplicationTests extends BaseApplicationTestsWithoutDb {
    private final InMemoryEventListener inMemoryEventListener;

    GlobalExceptionControllerApplicationTests(InMemoryEventListener inMemoryEventListener) {
        super();
        this.inMemoryEventListener = inMemoryEventListener;
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public JwtDecoder decoder() {
            return token -> {
                throw new IllegalArgumentException();
            };
        }
    }

    @Test
    void returns_problemDetail_when_an_exception_is_thrown_during_authentication() throws JSONException {
        // Act
        RestTestClient.ResponseSpec responseSpec = buildSessionRestTestClient()
            .get()
            .uri(EXCEPTION_PERMIT_ALL_GET_ENDPOINT)
            .header(HttpHeaders.COOKIE, "accessToken=x")
            .exchange();

        responseSpec
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody()
            .json(
                """
                {"type":"about:blank","title":"errors.internal_server_error","status":500,
                "detail":"errors.internal_server_error","instance":"/api/exception-handling/get"}""",
                jsonIgnoreArrayOrder
            );

        assertException(
            exception -> assertThat(exception)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .message()
                .isNull()
        );
    }

    @Test
    void throws_IllegalStateException_when_request_does_not_contain_an_exception() throws JSONException {
        // Act
        RestTestClient.ResponseSpec responseSpec = buildSessionRestTestClient()
            .get()
            .uri("/error")
            .exchange();

        responseSpec
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody()
            .json(
                """
                {"type":"about:blank","title":"errors.internal_server_error","status":500,
                "detail":"errors.internal_server_error","instance":"/api/error"}""",
                jsonIgnoreArrayOrder
            );

        assertException(
            exception -> assertThat(exception)
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot extract error from request")
        );
    }

    private void assertException(ThrowingConsumer<Exception> exceptionThrowingConsumer) {
        assertThat(inMemoryEventListener.getEvents(UnhandledExceptionEvent.class))
            .singleElement()
            .extracting(UnhandledExceptionEvent::exception)
            .satisfies(exceptionThrowingConsumer);
    }
}
