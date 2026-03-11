package com.theodo.springblueprint.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.theodo.springblueprint.common.api.events.UnhandledExceptionEvent;
import com.theodo.springblueprint.common.domain.events.Event;
import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;
import com.theodo.springblueprint.common.infra.adapters.SpringEventPublisher;

import com.theodo.springblueprint.testhelpers.helpers.InMemoryLogs;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.ThrowingConsumer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(
    {
        LoggingEventListener.class, // SUT
        SpringEventPublisher.class,
        InMemoryLogs.class
    }
)
@RequiredArgsConstructor
class LoggingEventListenerIntegrationTests {

    private final EventPublisherPort eventPublisher;
    private final InMemoryLogs inMemoryLogs;

    @BeforeEach
    public void startLogging() {
        inMemoryLogs.start();
    }

    @AfterEach
    public void stopLogging() {
        inMemoryLogs.stop();
    }

    @Test
    void publishing_an_exception_will_log_the_exception_once_with_error_level() {
        UnhandledExceptionEvent event = new UnhandledExceptionEvent(new Exception("This is an exception"));

        // Act
        eventPublisher.publishEvent(event);

        assertThat(inMemoryLogs.getCurrentThreadLogs())
            .satisfies(
                assertSingleLoggingEvent(
                    "java.lang.Exception: This is an exception",
                    LoggingEventListenerIntegrationTests.class,
                    ch.qos.logback.classic.Level.ERROR,
                    event.exception()
                )
            );

    }

    @Test
    void publishing_a_domain_event_will_log_the_event_once_with_info_level() {
        FakeEvent event = new FakeEvent();

        // Act
        eventPublisher.publishEvent(event);

        assertThat(inMemoryLogs.getCurrentThreadLogs())
            .satisfies(
                assertSingleLoggingEvent(
                    "This is a FakeEvent",
                    LoggingEventListenerIntegrationTests.class,
                    ch.qos.logback.classic.Level.INFO
                )
            );
    }

    @Test
    void publishing_an_exception_with_unknown_thrower_will_log_the_exception_with_UnhandledExceptionEvent_as_source() {
        Exception exception = getExceptionWithInvalidStack();
        UnhandledExceptionEvent event = new UnhandledExceptionEvent(exception);

        // Act
        eventPublisher.publishEvent(event);

        assertThat(inMemoryLogs.getCurrentThreadLogs())
            .satisfies(
                assertSingleLoggingEvent(
                    "java.lang.Exception: This is an exception",
                    UnhandledExceptionEvent.class,
                    ch.qos.logback.classic.Level.ERROR,
                    event.exception()
                )
            );
    }

    private static ThrowingConsumer<Iterable<? extends ILoggingEvent>> assertSingleLoggingEvent(String expectedMessage,
        Class<?> expectedLoggerClass, ch.qos.logback.classic.Level expectedLevel) {
        return assertSingleLoggingEvent(expectedMessage, expectedLoggerClass, expectedLevel, null);
    }

    private static ThrowingConsumer<Iterable<? extends ILoggingEvent>> assertSingleLoggingEvent(String expectedMessage,
        Class<?> expectedLoggerClass, ch.qos.logback.classic.Level expectedLevel,
        @Nullable Exception expectedException) {
        return l -> assertThat(l)
            .filteredOn(e -> expectedMessage.equals(e.getFormattedMessage()))
            .singleElement()
            .satisfies(e -> {
                assertThat(e.getLoggerName()).isEqualTo(expectedLoggerClass.getName());
                assertThat(e.getLevel()).isEqualTo(expectedLevel);
                if (expectedException != null) {
                    assertThat(((ThrowableProxy) e.getThrowableProxy()).getThrowable()).isSameAs(expectedException);
                }
            }
            );
    }

    private static Exception getExceptionWithInvalidStack() {
        Exception exception = new Exception("This is an exception");
        StackTraceElement invalidStackTrace = new StackTraceElement("InvalidClassName", "methodName", "fileName", 1);
        exception.setStackTrace(new StackTraceElement[] { invalidStackTrace });
        return exception;
    }

    private record FakeEvent() implements Event {
        @Override
        public String toString() {
            return "This is a FakeEvent";
        }

        @Override
        public Class<?> sourceType() {
            return LoggingEventListenerIntegrationTests.class;
        }
    }
}
