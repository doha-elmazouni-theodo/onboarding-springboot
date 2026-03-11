package com.theodo.springblueprint.common.api.exceptionhandling;

import static com.theodo.springblueprint.testhelpers.baseclasses.exceptionhandling.ExceptionHandlingFakeEndpoint.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.theodo.springblueprint.testhelpers.baseclasses.exceptionhandling.BaseExceptionHandlerIntegrationTests;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.ResultActions;

class CommonExceptionHandlerIntegrationTests extends BaseExceptionHandlerIntegrationTests {

    protected CommonExceptionHandlerIntegrationTests(BaseExceptionHandlerDependencies dependencies) {
        super(dependencies);
    }

    @Test
    void using_POST_on_a_GET_endpoint_returns_a_problemDetails_with_405() throws Exception {
        HttpStatus expectedHttpStatus = HttpStatus.METHOD_NOT_ALLOWED;
        String expectedJsonBody = """
            {"type":"about:blank","title":"Method Not Allowed","status":405,
            "detail":"Method 'POST' is not supported.","instance":"/exception-handling/get"}""";

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(EXCEPTION_PERMIT_ALL_GET_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
        );

        resultActions
            .andExpect(status().is(expectedHttpStatus.value()))
            .andExpect(jsonIgnoreArrayOrder(expectedJsonBody));
    }

    @Test
    void posting_a_body_that_fails_type_level_validation_return_a_problemDetails_with_400() throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(
            post(EXCEPTION_POST_WITH_BODY_FAILING_TYPE_LEVEL_VALIDATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        );

        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonIgnoreArrayOrder(
                    """
                    {"type":"about:blank","title":"errors.input_failed_validation",
                    "status":400,"detail":"errors.input_failed_validation",
                    "instance":"/exception-handling/complex/post",
                    "errorMetadata":{"errors":[{"constraint":"AlwaysFailingTypeLevelValidation",
                    "message":"Type-level validation failed"}]}}"""
                )
            );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRequestBodies")
    void posting_a_body_that_fails_field_level_validation_return_a_problemDetails_with_400(
        String requestBody,
        String expectedResponseBody) throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(
            post(EXCEPTION_POST_WITH_BODY_ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(requestBody)
        );

        resultActions.andExpect(status().isBadRequest()).andExpect(jsonIgnoreArrayOrder(expectedResponseBody));
    }

    private static Stream<Arguments> provideInvalidRequestBodies() {
        return Stream.of(
            // Empty JSON object
            Arguments.of(
                "{}",
                """
                {"type":"about:blank","title":"errors.input_failed_validation","status":400,"detail":"errors.input_failed_validation",
                "instance":"/exception-handling/post","errorMetadata":{"fields":[
                {"field":"notBlankString","constraint":"NotBlank","message":"must not be blank","rejectedValue":null},
                {"field":"requiredInt","constraint":"NotNull","message":"must not be null","rejectedValue":null}]}}"""
            ),
            // Invalid JSON request body
            Arguments.of(
                "invalid",
                """
                {"type":"about:blank","title":"Bad Request","status":400,
                "detail":"Failed to read request","instance":"/exception-handling/post"}"""
            ),
            // Missing required field
            Arguments.of(
                """
                {"requiredInt":10}""",
                """
                {"type":"about:blank","title":"errors.input_failed_validation","status":400,"detail":"errors.input_failed_validation",
                "instance":"/exception-handling/post","errorMetadata":{"fields":[
                {"field":"notBlankString","constraint":"NotBlank","message":"must not be blank","rejectedValue":null}]}}"""
            ),
            // Wrong data type
            Arguments.of(
                """
                {"requiredInt":"random string"}""",
                """
                {"type":"about:blank","title":"errors.input_bad_format","status":400,
                "detail":"errors.input_bad_format","instance":"/exception-handling/post"}"""
            ),
            // Empty request body
            Arguments.of(
                "",
                """
                {"type":"about:blank","title":"errors.missing_body","status":400,
                "detail":"errors.missing_body","instance":"/exception-handling/post"}"""
            )
        );
    }

    private static Stream<Arguments> staticProvideExceptions() {
        return Stream.of(
            Arguments.of(
                new AccessDeniedException(""),
                HttpStatus.UNAUTHORIZED,
                """
                {"type":"about:blank","title":"errors.unauthorized","status":401,
                "detail":"errors.unauthorized","instance":"/exception-handling/throw"}"""
            )
        );
    }

    @Override
    protected Stream<Arguments> getExceptions() {
        return staticProvideExceptions();
    }
}
