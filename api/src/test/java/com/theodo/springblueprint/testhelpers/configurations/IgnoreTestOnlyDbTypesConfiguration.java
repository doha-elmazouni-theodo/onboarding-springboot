package com.theodo.springblueprint.testhelpers.configurations;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;

import com.theodo.springblueprint.Application;
import com.theodo.springblueprint.testhelpers.annotations.IncludeTestOnlyDbTypes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.persistenceunit.ManagedClassNameFilter;

@ConditionalOnProperty(value = IncludeTestOnlyDbTypes.PROPERTY_NAME, havingValue = "false", matchIfMissing = true)
@TestConfiguration
@EnableJpaRepositories(
    basePackageClasses = Application.class,
    excludeFilters = { @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = BASE_PACKAGE_NAME + "..TestOnly*") }
)
@Import(IgnoreTestOnlyDbTypesConfiguration.IgnoreFilter.class)
public class IgnoreTestOnlyDbTypesConfiguration {

    static class IgnoreFilter implements ManagedClassNameFilter {

        @Override
        public boolean matches(String className) {
            return !className.contains(".TestOnly");
        }
    }
}
