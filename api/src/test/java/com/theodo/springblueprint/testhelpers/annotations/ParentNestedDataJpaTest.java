package com.theodo.springblueprint.testhelpers.annotations;

import com.theodo.springblueprint.testhelpers.junitextensions.ParentNestedSpringExtension;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.AutoConfigureDataJpa;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureJdbc;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.PropertyMapping;
import org.springframework.boot.test.context.filter.annotation.TypeExcludeFilters;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.test.context.BootstrapWith;
import org.springframework.transaction.annotation.Transactional;

// TLDR: Use when @Nested tests live in an abstract parent.
// JUnit treats the nested class as the test class; Spring builds the context from that class.
// Boot slices (e.g., DataJpaTest) hard-wire SpringExtension, so subclass config is ignored and beans go missing.
// This annotation forces context creation from the concrete subclass.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BootstrapWith(ParentNestedDataJpaTest.ParentNestedDataJpaTestContextBootstrapper.class)
@ExtendWith(ParentNestedSpringExtension.class)
@OverrideAutoConfiguration(enabled = false)
@TypeExcludeFilters(ParentNestedDataJpaTest.ParentNestedDataJpaTypeExcludeFilter.class)
@Transactional
@AutoConfigureDataJpa
@AutoConfigureJdbc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureTestEntityManager
@ImportAutoConfiguration
public @interface ParentNestedDataJpaTest {

    String[] properties() default {};

    @PropertyMapping("spring.jpa.show-sql")
    boolean showSql() default true;

    @PropertyMapping("spring.data.jpa.repositories.bootstrap-mode")
    BootstrapMode bootstrapMode() default BootstrapMode.DEFAULT;

    boolean useDefaultFilters() default true;

    ComponentScan.Filter[] includeFilters() default {};

    ComponentScan.Filter[] excludeFilters() default {};

    @AliasFor(annotation = ImportAutoConfiguration.class, attribute = "exclude")
    Class<?>[] excludeAutoConfiguration() default {};

    // Boot slice bootstrapper tied to ParentNestedDataJpaTest; DataJpaTest bootstrapper is package-private,
    // so we must provide our own to build the slice from the concrete subclass.
    final class ParentNestedDataJpaTestContextBootstrapper
        extends
            org.springframework.boot.test.autoconfigure.TestSliceTestContextBootstrapper<ParentNestedDataJpaTest> {
    }

    // Type-exclude filter for ParentNestedDataJpaTest; DataJpaTypeExcludeFilter is package-private,
    // so we mirror its behavior while allowing subclass context.
    final class ParentNestedDataJpaTypeExcludeFilter
        extends
            org.springframework.boot.test.context.filter.annotation.StandardAnnotationCustomizableTypeExcludeFilter<ParentNestedDataJpaTest> {

        public ParentNestedDataJpaTypeExcludeFilter(Class<?> testClass) {
            super(testClass);
        }
    }
}
