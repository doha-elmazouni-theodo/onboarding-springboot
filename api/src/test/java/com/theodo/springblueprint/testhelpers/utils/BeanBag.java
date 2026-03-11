package com.theodo.springblueprint.testhelpers.utils;

import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public class BeanBag {

    private final ImmutableSet<Object> beans;

    @NonNull public <T> T get(Class<T> beanType) {
        return beans.selectInstancesOf(beanType).getOnly();
    }
}
