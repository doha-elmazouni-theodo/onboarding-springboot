package com.theodo.springblueprint.testhelpers.annotations;

import com.theodo.springblueprint.common.infra.configurations.DataSourceProxyConfig;
import com.theodo.springblueprint.common.infra.configurations.HibernateEventListenersRegistrarBeanPostProcessor;
import com.theodo.springblueprint.testhelpers.configurations.IgnoreTestOnlyDbTypesConfiguration;
import com.theodo.springblueprint.testhelpers.junitextensions.SetupTestDatabaseExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SetupTestDatabaseExtension.class)
@ContextConfiguration(initializers = SetupTestDatabaseExtension.Initializer.class)
@Execution(ExecutionMode.SAME_THREAD) // TODO: investigate how to remove this constraint (default is CONCURRENT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@TestPropertySource(
    properties = {
        "spring.datasource.hikari.maximum-pool-size=10",
        "spring.datasource.hikari.minimum-idle=1",
        "spring.datasource.hikari.idle-timeout=1000",
    }
)
@Import(
    { HibernateEventListenersRegistrarBeanPostProcessor.class, IgnoreTestOnlyDbTypesConfiguration.class,
        DataSourceProxyConfig.class }
)
public @interface SetupDatabase {
}
