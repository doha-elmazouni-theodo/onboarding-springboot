package com.theodo.springblueprint.common.infra.database.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.theodo.springblueprint.common.infra.configurations.DataSourceProxyConfig;
import com.theodo.springblueprint.testhelpers.annotations.IncludeTestOnlyDbTypes;
import com.theodo.springblueprint.testhelpers.annotations.SetupDatabase;
import com.theodo.springblueprint.testhelpers.helpers.InMemoryLogs;
import jakarta.persistence.EntityManagerFactory;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SetupDatabase
@DataJpaTest
@IncludeTestOnlyDbTypes
@Import({ DataSourceProxyConfig.class, InMemoryLogs.class })
@RequiredArgsConstructor
class DatabaseQueryLoggingIntegrationTests {

    private final TestOnlyCommentedRepository repository;
    private final InMemoryLogs logs;
    private final EntityManagerFactory entityManagerFactory;

    @BeforeEach
    public void startLogging() {
        logs.start();
    }

    @AfterEach
    public void stopLogging() {
        logs.stop();
    }

    @Test
    void select_query_is_logged_with_comment_from_calling_method() {
        // Act
        repository.selectAll();

        String expectedCommentPrefix = expectedCommentPrefix(TestOnlyCommentedRepository.class, "selectAll");
        assertAnyMessageContainsAll(expectedCommentPrefix, "select");
    }

    @Test
    void insert_query_is_logged_with_comment_from_calling_method() {
        // Act
        repository.saveNewEntity();

        String expectedCommentPrefix = expectedCommentPrefix(TestOnlyCommentedRepository.class, "saveNewEntity");
        assertAnyMessageContainsAll(expectedCommentPrefix, "insert");
    }

    @Test
    void update_query_is_logged_with_comment_from_calling_method() {
        TestOnlyCommentedDbEntity entity = repository.saveNewEntity();

        // Act
        repository.updateNameField(entity);

        String expectedCommentPrefix = expectedCommentPrefix(TestOnlyCommentedRepository.class, "updateNameField");
        assertAnyMessageContainsAll(expectedCommentPrefix, "update");
    }

    @Test
    void delete_query_is_logged_with_comment_from_calling_method() {
        TestOnlyCommentedDbEntity entity = repository.saveNewEntity();

        // Act
        repository.deleteAndFlush(entity);

        String expectedCommentPrefix = expectedCommentPrefix(TestOnlyCommentedRepository.class, "deleteAndFlush");
        assertAnyMessageContainsAll(expectedCommentPrefix, "delete");
    }

    @Test
    void select_query_with_existing_comment_is_not_prefixed() {
        String existingComment = "/* already */";

        // Act
        entityManagerFactory.createEntityManager().createNativeQuery(existingComment + " select 1").getResultList();

        String unexpectedPrefix = expectedCommentPrefix(
            DatabaseQueryLoggingIntegrationTests.class,
            "select_query_with_existing_comment_is_not_prefixed"
        );

        assertThat(logs.getCurrentThreadLogs())
            .filteredOn(e -> e.getFormattedMessage().contains(existingComment))
            .singleElement()
            .satisfies(e -> assertThat(e.getFormattedMessage()).doesNotContain(unexpectedPrefix));
    }

    @Test
    void select_query_without_app_frame_has_no_comment_prefix() throws ExecutionException, InterruptedException {
        // Act
        ImmutableList<ILoggingEvent> externalLogs = runInExternalThread(() -> {
            entityManagerFactory.createEntityManager().createNativeQuery("select 1").getResultList();
            return logs.getCurrentThreadLogs();
        });

        assertThat(externalLogs)
            .anySatisfy(
                event -> assertThat(event.getFormattedMessage())
                    .contains("/* not triggered within the application code */")
            );
    }

    private void assertAnyMessageContainsAll(String... fragments) {
        assertThat(logs.getCurrentThreadLogs())
            .extracting(ILoggingEvent::getFormattedMessage)
            .anySatisfy(
                message -> assertThat(message)
                    .contains(fragments)
            );
    }

    private <T> T runInExternalThread(Callable<T> task) throws ExecutionException, InterruptedException {
        try (
            ExecutorService executor = Executors
                .newSingleThreadExecutor(runnable -> new Thread(runnable, "external-query-runner"))
        ) {
            Future<T> result = executor.submit(task);
            return result.get();
        }
    }

    private String expectedCommentPrefix(Class<?> owner, String methodName) {
        return "/* %s#%s".formatted(owner.getName(), methodName);
    }

    @Nested
    @TestPropertySource(properties = "logging.level.DatabaseQueryLogger=INFO")
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    class DebugDisabledLogging {

        @Test
        void query_is_not_logged_when_debug_is_disabled() {

            // Act
            repository.findAll();

            assertThat(logs.getCurrentThreadLogs())
                .noneSatisfy(event -> assertThat(event.getLoggerName()).isEqualTo("DatabaseQueryLogger"));
        }
    }
}
