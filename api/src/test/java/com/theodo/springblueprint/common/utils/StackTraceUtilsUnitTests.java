package com.theodo.springblueprint.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.theodo.springblueprint.Application;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@UnitTest
class StackTraceUtilsUnitTests {

    @Nested
    class ClosestAppStackTraceElement {

        @Test
        void returns_calling_method() {

            // Act
            StackTraceElement element = StackTraceUtils.closestAppStackTraceElement(
                Application.BASE_PACKAGE_NAME,
                StackTraceUtils.class
            );

            assertStackTraceElementInThisClass(element, "returns_calling_method");
        }

        @Test
        void skips_ignored_class() {

            // Act
            StackTraceElement element = new IgnoredCaller().run(
                () -> StackTraceUtils.closestAppStackTraceElement(
                    Application.BASE_PACKAGE_NAME,
                    IgnoredCaller.class
                )
            );

            assertStackTraceElementInThisClass(element, "skips_ignored_class");
        }

        @Test
        void skips_lambda_methods() {
            AtomicReference<@Nullable StackTraceElement> holder = new AtomicReference<>();
            Runnable action = () -> holder.set(
                StackTraceUtils.closestAppStackTraceElement(
                    Application.BASE_PACKAGE_NAME,
                    StackTraceUtils.class
                )
            );

            // Act
            action.run();

            StackTraceElement element = holder.get();

            assertStackTraceElementInThisClass(element, "skips_lambda_methods");
        }

        @Test
        void skips_double_dollar_class_names() {

            // Act
            StackTraceElement element = new Proxy$$Caller().run(
                () -> StackTraceUtils.closestAppStackTraceElement(
                    Application.BASE_PACKAGE_NAME,
                    StackTraceUtils.class
                )
            );

            assertStackTraceElementInThisClass(element, "skips_double_dollar_class_names");
        }

        @Test
        void returns_nested_caller_method() {

            // Act
            StackTraceElement element = new NestedCaller().run(
                () -> StackTraceUtils.closestAppStackTraceElement(
                    Application.BASE_PACKAGE_NAME,
                    StackTraceUtils.class
                )
            );

            assertStackTraceElement(element, NestedCaller.class, "run");
        }

        @Test
        void returns_null_when_package_mismatch() {

            // Act
            StackTraceElement element = StackTraceUtils.closestAppStackTraceElement(
                "not.matching",
                StackTraceUtils.class
            );

            assertThat(element).isNull();
        }

        private void assertStackTraceElement(
            @Nullable StackTraceElement element,
            Class<?> expectedClass,
            String expectedMethodName) {
            assertThat(element)
                .isNotNull()
                .extracting(StackTraceElement::getClassName, StackTraceElement::getMethodName)
                .containsExactly(expectedClass.getName(), expectedMethodName);
        }

        private void assertStackTraceElementInThisClass(@Nullable StackTraceElement element,
            String expectedMethodName) {
            assertStackTraceElement(element, getClass(), expectedMethodName);
        }

        static class IgnoredCaller {

            @Nullable StackTraceElement run(Supplier<@Nullable StackTraceElement> action) {
                return action.get();
            }
        }

        @SuppressWarnings({ "PMD.AvoidDollarSigns", "PMD.ClassNamingConventions" })
        static class Proxy$$Caller {

            @Nullable StackTraceElement run(Supplier<@Nullable StackTraceElement> action) {
                return action.get();
            }
        }

        static class NestedCaller {

            @Nullable StackTraceElement run(Supplier<@Nullable StackTraceElement> action) {
                return action.get();
            }
        }
    }

    @Nested
    class SourceTypeFromException {

        @Test
        void returns_exception_origin() {
            Exception exception = new Exception("boom");

            // Act
            Class<?> result = StackTraceUtils.sourceTypeFromException(exception).orElseThrow();

            assertThat(result).isEqualTo(SourceTypeFromException.class);
        }

        @Test
        void returns_fallback_when_stack_is_empty() {
            Exception exception = new Exception("boom");
            exception.setStackTrace(new StackTraceElement[0]);

            // Act
            var sourceTypeOptional = StackTraceUtils.sourceTypeFromException(exception);

            assertThat(sourceTypeOptional).isEmpty();
        }

        @Test
        void returns_fallback_when_class_is_missing() {
            Exception exception = new Exception("boom");
            exception.setStackTrace(
                new StackTraceElement[] {
                    new StackTraceElement("not.existing.ClassName", "method", "file", 12)
                }
            );

            // Act
            var sourceTypeOptional = StackTraceUtils.sourceTypeFromException(exception);

            assertThat(sourceTypeOptional).isEmpty();
        }
    }
}
