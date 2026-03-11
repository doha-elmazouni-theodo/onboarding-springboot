package com.theodo.springblueprint.testhelpers.baseclasses;

import com.theodo.springblueprint.testhelpers.configurations.IgnoreTestOnlyDbTypesConfiguration;
import com.theodo.springblueprint.testhelpers.junitextensions.SetupTestDatabaseExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(IgnoreTestOnlyDbTypesConfiguration.class)
public abstract class BaseApplicationTestsWithDb extends AbstractApplicationTests {

    @Container
    private static final JdbcDatabaseContainer<?> testDatabase = SetupTestDatabaseExtension
        .createJdbcDatabaseContainer();

    protected BaseApplicationTestsWithDb() {
        super();
    }

    @DynamicPropertySource
    private static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        // I couldn't make the transaction rollback work when using @SpringBootTest
        // with WebEnvironment.RANDOM_PORT. We need rollback to make tests independent and isolated.
        // So the alternative solution is to drop the database and recreate it before each test method
        if (testDatabase.isCreated()) {
            testDatabase.close();
        }
        testDatabase.start();
        registry.add("spring.datasource.url", testDatabase::getJdbcUrl);
        registry.add("spring.datasource.username", testDatabase::getUsername);
        registry.add("spring.datasource.password", testDatabase::getPassword);
        registry.add("spring.liquibase.enabled", () -> false);
    }
}
