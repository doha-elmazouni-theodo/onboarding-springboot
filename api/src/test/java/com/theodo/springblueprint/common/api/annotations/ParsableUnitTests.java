package com.theodo.springblueprint.common.api.annotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.common.utils.ParsableChecker;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import jakarta.validation.ConstraintDefinitionException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@UnitTest
class ParsableUnitTests {

    private static final String VALID_VALUE = "15";
    private static final String INVALID_VALUE = "fifteen";

    private final Validator validator;

    ParsableUnitTests() {
        try (LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean()) {
            factoryBean.afterPropertiesSet();
            validator = factoryBean.getValidator();
        }
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_no_violation_on_valid_objects(Function<String, Object> factory) {
        Object validObject = factory.apply(VALID_VALUE);

        // Act
        Set<ConstraintViolation<Object>> violations = validator.validate(validObject);

        assertThat(violations).as("There should be no validation errors for valid input.").isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_violations_on_types_with_invalid_field_value(Function<String, Object> factory) {
        Object invalidObject = factory.apply(INVALID_VALUE);

        // Act
        Set<ConstraintViolation<Object>> violations = validator.validate(invalidObject);

        assertThat(violations).as("There should be validation errors for invalid input.").isNotEmpty();
        ConstraintViolation<Object> violation = violations.iterator().next();

        assertThat(violation)
            .returns(Parsable.class, v -> v.getConstraintDescriptor().getAnnotation().annotationType())
            .returns("Invalid value", v -> v.getMessage())
            .returns("field1", v -> v.getPropertyPath().toString())
            .returns(INVALID_VALUE, v -> v.getInvalidValue());
    }

    @Test
    void validator_throws_if_checker_does_not_have_default_constructor() {
        InputRecordCheckerWithoutDefaultConstructor request = new InputRecordCheckerWithoutDefaultConstructor(
            VALID_VALUE
        );

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> validator.validate(request));

        exceptionAssertion
            .isInstanceOf(ValidationException.class)
            .hasCauseInstanceOf(ConstraintDefinitionException.class);
    }

    @Test
    void validator_returns_no_violation_when_value_is_null() {
        InputRecord input = new InputRecord(null);

        // Act
        Set<ConstraintViolation<InputRecord>> violations = validator.validate(input);

        assertThat(violations).as("A null value should be valid by default.").isEmpty();
    }

    private static Stream<Arguments> provideObjectFactories() {
        return Stream.of(
            Arguments.of(constructorFunction(InputClass::new)),
            Arguments.of(constructorFunction(InputRecord::new)),
            Arguments.of(constructorFunction(InputRecordCheckerWithValidInstanceField::new)),
            Arguments.of(constructorFunction(InputRecordCheckerWithInstanceFieldOfBadType::new)),
            Arguments.of(constructorFunction(InputRecordCheckerWithPrivateInstanceField::new)),
            Arguments.of(constructorFunction(InputRecordCheckerWithNonStaticInstanceField::new)),
            Arguments.of(constructorFunction(InputRecordCheckerWithNonFinalInstanceField::new))
        );
    }

    private static <T> Function<String, T> constructorFunction(Function<String, T> constructor) {
        return constructor;
    }

    @Getter
    @RequiredArgsConstructor
    private static final class InputClass {

        @Parsable(Checker.class)
        private final String field1;
    }

    private record InputRecord(@Parsable(Checker.class) @Nullable String field1) {
    }

    static final class Checker implements ParsableChecker {

        @Override
        public boolean canParse(Object value) {
            return VALID_VALUE.equals(value);
        }
    }

    private record InputRecordCheckerWithoutDefaultConstructor(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            public Checker(String param) {
                param.notify();
            }

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithValidInstanceField(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            public static final Checker INSTANCE = new Checker();

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithInstanceFieldOfBadType(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            public static final String INSTANCE = "abc";

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithPrivateInstanceField(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            @SuppressWarnings("all")
            private static final Checker INSTANCE = new Checker();

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithNonStaticInstanceField(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            @SuppressWarnings("PMD.FinalFieldCouldBeStatic")
            @Nullable public final Checker INSTANCE = null;

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithNonFinalInstanceField(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            @SuppressWarnings("PMD.MutableStaticState")
            public static Checker INSTANCE = new Checker();

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }
}
