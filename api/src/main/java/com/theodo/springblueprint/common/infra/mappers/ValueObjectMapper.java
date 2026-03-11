package com.theodo.springblueprint.common.infra.mappers;

import com.theodo.springblueprint.common.utils.collections.Immutable;
import org.eclipse.collections.api.set.ImmutableSet;

public interface ValueObjectMapper<TValueObject, TPrimitive> {
    TPrimitive fromValueObject(TValueObject valueObject);

    TValueObject toValueObject(TPrimitive primitiveValue);

    default ImmutableSet<TPrimitive> fromValueObjects(Iterable<TValueObject> valueObjects) {
        return Immutable.collectSet(valueObjects, this::fromValueObject);
    }

    default ImmutableSet<TValueObject> toValueObjects(Iterable<TPrimitive> primitiveValues) {
        return Immutable.collectSet(primitiveValues, this::toValueObject);
    }
}
