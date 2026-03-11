package com.theodo.springblueprint.common.utils;

import jakarta.annotation.Nullable;
import java.util.Optional;

public interface StackTraceUtils {

    @Nullable static StackTraceElement closestAppStackTraceElement(String basePackageName, Class<?> ignoredClass) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String ignoredClassName = ignoredClass.getName();

        for (StackTraceElement frame : stack) {
            String className = frame.getClassName();

            if (!className.startsWith(basePackageName)) {
                continue;
            }

            if (className.equals(StackTraceUtils.class.getName())) {
                continue;
            }

            if (className.equals(ignoredClassName)) {
                continue;
            }

            if (className.contains("$$")) {
                continue;
            }

            String methodName = frame.getMethodName();
            if (methodName.startsWith("lambda$")) {
                continue;
            }

            return frame;
        }

        return null;
    }

    static Optional<Class<?>> sourceTypeFromException(Exception exception) {
        try {
            StackTraceElement[] stackTraceElements = exception.getStackTrace();
            if (stackTraceElements.length == 0) {
                return Optional.empty();
            }
            String className = stackTraceElements[0].getClassName();
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }
    }
}
