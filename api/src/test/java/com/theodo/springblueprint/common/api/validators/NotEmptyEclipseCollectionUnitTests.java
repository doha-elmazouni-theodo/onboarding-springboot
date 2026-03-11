package com.theodo.springblueprint.common.api.validators;

import static org.assertj.core.api.Assertions.assertThat;

import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@UnitTest
class NotEmptyEclipseCollectionUnitTests {

    private static final ImmutableList<String> NON_EMPTY_LIST = Immutable.list.of("A");
    private static final ImmutableList<String> EMPTY_LIST = Immutable.list.empty();

    private final Validator validator;

    NotEmptyEclipseCollectionUnitTests() {
        try (LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean()) {
            factoryBean.afterPropertiesSet();
            validator = factoryBean.getValidator();
        }
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_no_violation_on_non_empty_collections(Function<ImmutableList<String>, ?> factory) {
        Object target = factory.apply(NON_EMPTY_LIST);

        // Act
        Set<ConstraintViolation<@Nullable Object>> violations = validator.validate(target);

        assertThat(violations).as("non-empty collections must pass validation").isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_violation_on_empty_collections(Function<ImmutableList<String>, ?> factory) {
        Object target = factory.apply(EMPTY_LIST);

        // Act
        Set<ConstraintViolation<@Nullable Object>> violations = validator.validate(target);

        assertThat(violations).as("empty collections must fail validation").isNotEmpty();

        ConstraintViolation<@Nullable Object> violation = violations.iterator().next();
        assertThat(violation)
            .returns(NotEmpty.class, v -> v.getConstraintDescriptor().getAnnotation().annotationType())
            .returns("must not be empty", v -> v.getMessage())
            .returns("field1", v -> v.getPropertyPath().toString())
            .returns(EMPTY_LIST, v -> v.getInvalidValue());
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_violation_on_null_collections(Function<@Nullable ImmutableList<String>, ?> factory) {
        Object target = factory.apply(null);

        // Act
        Set<ConstraintViolation<@Nullable Object>> violations = validator.validate(target);

        assertThat(violations).as("null collections must fail validation").isNotEmpty();

        ConstraintViolation<@Nullable Object> violation = violations.iterator().next();
        assertThat(violation)
            .as("NotEmpty violation details")
            .returns(NotEmpty.class, v -> v.getConstraintDescriptor().getAnnotation().annotationType())
            .returns("must not be empty", v -> v.getMessage())
            .returns("field1", v -> v.getPropertyPath().toString())
            .returns(null, v -> v.getInvalidValue());
    }

    private static Stream<Arguments> provideObjectFactories() {
        return Stream.of(
            Arguments.of((Function<ImmutableList<String>, ?>) InputClass::new),
            Arguments.of((Function<ImmutableList<String>, ?>) InputRecord::new)
        );
    }

    @Getter
    @RequiredArgsConstructor
    private static final class InputClass {

        @NotEmpty private final ImmutableList<String> field1;
    }

    private record InputRecord(@NotEmpty ImmutableList<String> field1) {
    }
}
