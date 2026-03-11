package com.theodo.springblueprint.testhelpers.junitextensions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import java.lang.reflect.Proxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SuppressWarnings("PMD.TooManyMethods")
public final class ParentNestedSpringExtension
    implements
        BeforeAllCallback,
        AfterAllCallback,
        TestInstancePostProcessor,
        BeforeEachCallback,
        AfterEachCallback,
        BeforeTestExecutionCallback,
        AfterTestExecutionCallback,
        ParameterResolver {

    private final SpringExtension springExtension = new SpringExtension();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        springExtension.beforeAll(wrap(context));
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        springExtension.afterAll(wrap(context));
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        springExtension.postProcessTestInstance(testInstance, wrap(context));
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        springExtension.beforeEach(wrap(context));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        springExtension.afterEach(wrap(context));
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        springExtension.beforeTestExecution(wrap(context));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        springExtension.afterTestExecution(wrap(context));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return springExtension.supportsParameter(parameterContext, wrap(extensionContext));
    }

    @Override
    @Nullable public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return springExtension.resolveParameter(parameterContext, wrap(extensionContext));
    }

    private ExtensionContext wrap(ExtensionContext context) {
        Class<?> testClass = findEnclosingTestClass(context);
        InvocationHandler handler = new EnclosingContextInvocationHandler(context, testClass);
        return (ExtensionContext) Proxy.newProxyInstance(
            ExtensionContext.class.getClassLoader(),
            new Class<?>[] { ExtensionContext.class },
            handler
        );
    }

    private Class<?> findEnclosingTestClass(ExtensionContext context) {
        for (ExtensionContext current = context; current != null; current = current.getParent().orElse(null)) {
            Optional<Class<?>> candidate = current.getTestClass()
                .filter(ParentNestedSpringExtension::isEnclosingTestClass);
            if (candidate.isPresent()) {
                return candidate.orElseThrow();
            }
        }
        return context.getRequiredTestClass();
    }

    private static boolean isEnclosingTestClass(Class<?> candidateClass) {
        return !candidateClass.isAnnotationPresent(Nested.class)
            && !Modifier.isAbstract(candidateClass.getModifiers());
    }

    private static final class EnclosingContextInvocationHandler implements InvocationHandler {

        private final ExtensionContext delegate;
        private final Class<?> testClass;

        private EnclosingContextInvocationHandler(ExtensionContext delegate, Class<?> testClass) {
            this.delegate = delegate;
            this.testClass = testClass;
        }

        @Override
        @Nullable public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (isEqualsInvocation(method, args)) {
                return proxy == args[0];
            }
            if (method.getParameterCount() == 0) {
                return handleZeroArgMethod(proxy, method, args);
            }
            return invokeDelegate(method, args);
        }

        private boolean isEqualsInvocation(Method method, Object... args) {
            return "equals".equals(method.getName())
                && method.getParameterCount() == 1
                && args.length == 1;
        }

        @Nullable private Object handleZeroArgMethod(Object proxy, Method method, Object... args) throws Throwable {
            return switch (method.getName()) {
                case "getTestClass" -> Optional.of(testClass);
                case "getRequiredTestClass" -> testClass;
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> delegate + " (enclosingTestClass=" + testClass.getName() + ")";
                default -> invokeDelegate(method, args);
            };
        }

        @Nullable private Object invokeDelegate(Method method, Object... args) throws Throwable {
            try {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause();
                if (cause == null) {
                    throw exception;
                }
                throw cause;
            }
        }
    }
}
