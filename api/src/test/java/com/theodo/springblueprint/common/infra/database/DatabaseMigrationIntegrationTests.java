package com.theodo.springblueprint.common.infra.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.testhelpers.configurations.IgnoreTestOnlyDbTypesConfiguration;
import com.theodo.springblueprint.testhelpers.junitextensions.FailFastExtension;
import com.theodo.springblueprint.testhelpers.junitextensions.SetupTestDatabaseExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Optional;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(IgnoreTestOnlyDbTypesConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ANNOTATED) // Needed for @TempDir
@SuppressWarnings("PMD.ExcessiveImports")
@ActiveProfiles("schema-tools")
class DatabaseMigrationIntegrationTests {

    @Container
    private static final JdbcDatabaseContainer<?> testDatabase = SetupTestDatabaseExtension
        .createJdbcDatabaseContainer();

    private static final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
    private static final String changeLogFile = "db/changelog-master.yaml";
    private static final String HIBERNATE_REFERENCE_SCHEMA = "hibernate_reference";

    private final DataSource dataSource;
    private final Path tempDir;
    private final SchemaToolsRunner runner;

    DatabaseMigrationIntegrationTests(
        @Autowired DataSource dataSource,
        @TempDir Path tempDir,
        @Autowired SchemaToolsRunner runner) {
        this.dataSource = dataSource;
        this.tempDir = tempDir;
        this.runner = runner;
    }

    @TestConfiguration
    static class Configuration {

        @Bean
        SchemaToolsRunner schemaToolsRunner(SessionFactory sessionFactory) {
            ConfigurableApplicationContext applicationContext = new StaticApplicationContext();
            return new SchemaToolsRunner(sessionFactory, applicationContext);
        }
    }

    @Test
    @Order(1)
    void liquibase_generate_non_empty_diff_against_fresh_database() throws Exception {
        Optional<String> diffAsYamlOptional;
        try (Connection connection = dataSource.getConnection()) {
            Database targetDatabase = getDatabase(connection);
            Database referenceDatabase = getHibernateReferenceDatabase(connection);

            // Act
            diffAsYamlOptional = getDiffAsYaml(referenceDatabase, targetDatabase);
        }
        assertThat(diffAsYamlOptional)
            .as("diff on fresh database is expected to have content, but it was empty.")
            .isPresent();
    }

    @Test
    @Order(2)
    void liquibase_generate_empty_diff_after_applying_all_migrations() throws Exception {
        Optional<String> diffAsYamlOptional;
        try (Connection connection = dataSource.getConnection()) {
            Database targetDatabase = getDatabase(connection);
            applyMigrations(targetDatabase);
            Database referenceDatabase = getHibernateReferenceDatabase(connection);

            // Act
            diffAsYamlOptional = getDiffAsYaml(referenceDatabase, targetDatabase);
        }
        assertThat(diffAsYamlOptional)
            .withFailMessage(
                () -> "diff after applying all migrations is expected to be empty, but found this instead:\n\n" +
                    diffAsYamlOptional.orElseThrow()
            )
            .isEmpty();
    }

    @Test
    @Order(3)
    void hibernate_successfully_validate_schema_created_by_liquibase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Database targetDatabase = getDatabase(connection);
            applyMigrations(targetDatabase);
        }

        // Act
        var codeAssertion = assertThatCode(this::validateDefaultSchema);

        codeAssertion
            .doesNotThrowAnyException();
    }

    @Test
    @Order(4)
    void schema_tools_runner_throws_when_operation_argument_missing() {
        // Act
        var exceptionAssertion = assertThatThrownBy(
            () -> runner.run(new DefaultApplicationArguments("--invalid=invalid"))
        );

        exceptionAssertion
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(5)
    void schema_tools_runner_throws_when_operation_value_missing() {
        // Act
        var exceptionAssertion = assertThatThrownBy(
            () -> runner.run(new DefaultApplicationArguments("--schema-tools.operation"))
        );

        exceptionAssertion
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(6)
    void schema_tools_runner_ignores_unknown_operation_value() {
        // Act
        var codeAssertion = assertThatCode(
            () -> runner.run(new DefaultApplicationArguments("--schema-tools.operation=invalid"))
        );

        codeAssertion
            .doesNotThrowAnyException();
    }

    @DynamicPropertySource
    private static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        if (testDatabase.isCreated()) {
            testDatabase.close();
        }
        testDatabase.start();
        registry.add("spring.datasource.url", testDatabase::getJdbcUrl);
        registry.add("spring.datasource.username", testDatabase::getUsername);
        registry.add("spring.datasource.password", testDatabase::getPassword);
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    private Database getHibernateReferenceDatabase(Connection connection) throws DatabaseException {
        createHibernateReferenceSchema();
        Database referenceDatabase = getDatabase(connection);
        referenceDatabase.setDefaultSchemaName(HIBERNATE_REFERENCE_SCHEMA);
        return referenceDatabase;
    }

    private static Database getDatabase(Connection connection) throws DatabaseException {
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    private static void applyMigrations(Database targetDatabase) throws LiquibaseException {
        Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, targetDatabase);
        liquibase.update(new Contexts());
    }

    private Optional<String> getDiffAsYaml(Database referenceDatabase, Database targetDatabase)
        throws LiquibaseException, IOException, ParserConfigurationException {
        Path diffFile = tempDir.resolve("diff.postgresql.sql");
        DiffOutputControl diffOutputControl = new DiffOutputControl(false, false, false, null).addIncludedSchema(
            new CatalogAndSchema(null, null)
        );
        ObjectChangeFilter objectChangeFilter = new StandardObjectChangeFilter(
            StandardObjectChangeFilter.FilterType.EXCLUDE,
            "table:testonly_.+"
        );
        CommandLineUtils.doDiffToChangeLog(
            diffFile.toAbsolutePath().toString(),
            referenceDatabase,
            targetDatabase,
            null,
            diffOutputControl,
            objectChangeFilter,
            null,
            null,
            "none",
            "none"
        );
        if (Files.notExists(diffFile)) {
            return Optional.empty();
        }
        return Optional.of(Files.readString(diffFile));
    }

    private void validateDefaultSchema() {
        runner.run(new DefaultApplicationArguments("--schema-tools.operation=validate_default_schema"));
    }

    private void createHibernateReferenceSchema() {
        runner.run(
            new DefaultApplicationArguments(
                "--schema-tools.operation=export", "--schema-tools.export_schema=" + HIBERNATE_REFERENCE_SCHEMA
            )
        );
    }
}
