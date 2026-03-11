package com.theodo.springblueprint.testhelpers.baseclasses.exceptionhandling;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;
import static com.theodo.springblueprint.testhelpers.baseclasses.exceptionhandling.ExceptionHandlingFakeEndpoint.EXCEPTION_GET_ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.theodo.springblueprint.common.api.events.UnhandledExceptionEvent;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseWebMvcIntegrationTests;
import com.theodo.springblueprint.testhelpers.helpers.InMemoryEventListener;
import com.theodo.springblueprint.testhelpers.utils.ClassFinder;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ExceptionHandlingFakeEndpoint.class)
@Import(
    {
        InMemoryEventListener.class,
        BaseExceptionHandlerIntegrationTests.BaseExceptionHandlerIntegrationTestConfiguration.class,
    }
)
public abstract class BaseExceptionHandlerIntegrationTests extends BaseWebMvcIntegrationTests {

    protected static final String UNAUTHORIZED_JSON_RESPONSE = """
        {"type":"about:blank","title":"errors.unauthorized","status":401,
        "detail":"errors.unauthorized","instance":"/exception-handling/throw"}""";

    protected static final String INTERNAL_SERVER_ERROR_JSON_RESPONSE = """
        {"type":"about:blank","title":"errors.internal_server_error","status":500,
        "detail":"errors.internal_server_error","instance":"/exception-handling/throw"}""";

    private final ExceptionHandlingFakeEndpoint fakeEndpoint;
    private final InMemoryEventListener inMemoryEventListener;

    protected record BaseExceptionHandlerDependencies(
        ExceptionHandlingFakeEndpoint fakeEndpoint,
        InMemoryEventListener inMemoryEventListener,
        BaseWebMvcDependencies baseWebMvcDependencies
    ) {
    }

    protected BaseExceptionHandlerIntegrationTests(BaseExceptionHandlerDependencies dependencies) {
        super(dependencies.baseWebMvcDependencies());
        this.fakeEndpoint = dependencies.fakeEndpoint();
        this.inMemoryEventListener = dependencies.inMemoryEventListener();
    }

    @ParameterizedTest
    @MethodSource("staticProvideExceptions")
    void get_on_a_endpoint_that_throws_returns_expected_problemDetails_with_expected_http_code(
        Exception exceptionToThrow,
        HttpStatus expectedHttpStatus,
        String expectedJsonBody) throws Exception {
        fakeEndpoint.exceptionToThrow(exceptionToThrow);

        // Act
        ResultActions resultActions = mockMvc.perform(get(EXCEPTION_GET_ENDPOINT));

        resultActions
            .andExpect(status().is(expectedHttpStatus.value()))
            .andExpect(jsonIgnoreArrayOrder(expectedJsonBody));
        ImmutableList<UnhandledExceptionEvent> events = inMemoryEventListener.getEvents(UnhandledExceptionEvent.class);
        assertThat(events)
            .singleElement()
            .returns(exceptionToThrow, UnhandledExceptionEvent::exception);
    }

    @Test
    void staticProvideExceptions_returns_exactly_all_custom_exceptions_under_parent_package() {
        ImmutableList<? extends Class<?>> definedExceptions = ClassFinder.findAllNonAbstractExceptions(
            ClassFinder.getParentPackage(getClass())
        );

        // Act
        List<Class<?>> testedExceptionsUnderBasePackage = getExceptions()
            .<Class<?>>map(arg -> castNonNull(arg.get()[0]).getClass())
            // Ignore third party exceptions
            .filter(c -> c.getPackageName().startsWith(BASE_PACKAGE_NAME))
            .toList();

        assertThat(testedExceptionsUnderBasePackage)
            .as("staticProvideExceptions() must return exactly all custom exceptions under the parent package")
            .containsExactlyInAnyOrderElementsOf(definedExceptions);
    }

    // Implementation of this method must always return staticProvideExceptions().
    // This is needed because staticProvideExceptions must be static (junit constraint)
    // and static method cannot be abstract.
    protected abstract Stream<Arguments> getExceptions();

    @TestConfiguration
    static class BaseExceptionHandlerIntegrationTestConfiguration {

        @Bean
        BaseExceptionHandlerDependencies baseExceptionHandlerDependencies(
            ExceptionHandlingFakeEndpoint fakeEndpoint,
            InMemoryEventListener inMemoryEventListener,
            BaseWebMvcDependencies baseWebMvcDependencies) {
            return new BaseExceptionHandlerDependencies(fakeEndpoint, inMemoryEventListener, baseWebMvcDependencies);
        }
    }
}
