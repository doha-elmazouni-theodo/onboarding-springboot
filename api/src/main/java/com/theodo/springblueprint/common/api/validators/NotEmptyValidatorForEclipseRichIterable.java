package com.theodo.springblueprint.common.api.validators;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;
import org.eclipse.collections.api.RichIterable;

public class NotEmptyValidatorForEclipseRichIterable implements ConstraintValidator<NotEmpty, RichIterable<?>> {

    @Override
    public boolean isValid(@Nullable RichIterable<?> value, ConstraintValidatorContext ctx) {
        return value != null && value.notEmpty();
    }
}
