package com.theodo.springblueprint.testhelpers.utils;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.NamedThreadLocal;

// fork of org.springframework.context.support.SimpleThreadScope
public final class ClearableSimpleThreadScope implements Scope {

    @SuppressWarnings("nullness:type.argument")
    private final ThreadLocal<Map<String, Object>> threadScope = NamedThreadLocal.withInitial(
        "ClearableSimpleThreadScope",
        HashMap::new
    );

    @Override
    public @NonNull Object get(@NotNull String name, @NotNull ObjectFactory<?> objectFactory) {
        Map<String, Object> scope = this.threadScope.get();
        Object scopedObject = scope.get(name);
        if (scopedObject == null) {
            Object object = objectFactory.getObject();
            if (object == null) {
                throw new IllegalStateException("The ObjectFactory for '" + name + "' returned null");
            }
            scopedObject = object;
            scope.put(name, scopedObject);
        }
        return scopedObject;
    }

    @Override
    @Nullable public Object remove(@NotNull String name) {
        Map<String, Object> scope = this.threadScope.get();
        return scope.remove(name);
    }

    @Override
    public void registerDestructionCallback(@NotNull String name, @NotNull Runnable callback) {
    }

    @Override
    @Nullable public Object resolveContextualObject(@NotNull String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return Thread.currentThread().getName();
    }

    public void clear() {
        threadScope.get().clear();
    }
}
