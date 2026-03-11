package com.theodo.springblueprint.common.api.annotations;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

import com.theodo.springblueprint.common.api.annotations.Parsable.ParsableConstraintValidator;
import com.theodo.springblueprint.common.utils.ParsableChecker;
import jakarta.annotation.Nullable;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintDefinitionException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Optional;

@SuppressWarnings("unused")
@Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ParsableConstraintValidator.class)
public @interface Parsable {
    String message() default "Invalid value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends ParsableChecker> value();

    class ParsableConstraintValidator implements ConstraintValidator<Parsable, String> {

        @Nullable private ParsableChecker parsableChecker;

        @Override
        public void initialize(Parsable annotation) {
            parsableChecker = getChecker(annotation.value());
        }

        @Override
        public boolean isValid(@Nullable String value, ConstraintValidatorContext context) {
            return value == null || castNonNull(parsableChecker).canParse(value);
        }

        private static ParsableChecker getChecker(Class<? extends ParsableChecker> mapperClass) {
            return getSingleton(mapperClass).orElseGet(() -> getNewInstance(mapperClass));
        }

        private static ParsableChecker getNewInstance(Class<? extends ParsableChecker> mapperClass) {
            try {
                return mapperClass.getDeclaredConstructor().newInstance();
            } catch (
                NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e
            ) {
                throw new ConstraintDefinitionException("Failed to instantiate ParsableChecker: " + mapperClass, e);
            }
        }

        private static Optional<ParsableChecker> getSingleton(Class<? extends ParsableChecker> mapperClass) {
            try {
                Field instanceField = mapperClass.getDeclaredField("INSTANCE");
                ParsableChecker result = null;
                int modifiers = instanceField.getModifiers();
                if (
                    instanceField.getType().equals(mapperClass) &&
                        Modifier.isPublic(modifiers) &&
                        Modifier.isStatic(modifiers) &&
                        Modifier.isFinal(modifiers)
                ) {
                    result = (ParsableChecker) instanceField.get(mapperClass);
                }
                return Optional.ofNullable(result);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
}
