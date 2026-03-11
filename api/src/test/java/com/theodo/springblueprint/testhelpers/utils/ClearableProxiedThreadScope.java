package com.theodo.springblueprint.testhelpers.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.NamedThreadLocal;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

// fork of org.springframework.context.support.SimpleThreadScope
public final class ClearableProxiedThreadScope implements Scope {

    @SuppressWarnings("nullness:type.argument")
    private final ThreadLocal<Map<String, BeanData>> threadScope = NamedThreadLocal.withInitial(
        "ClearableProxiedThreadScope",
        HashMap::new
    );

    @Override
    public @NonNull Object get(@NotNull String name, @NotNull ObjectFactory<?> objectFactory) {
        Map<String, BeanData> beanDataMap = this.threadScope.get();
        Object target = objectFactory.getObject();
        if (target == null) {
            throw new IllegalStateException("The ObjectFactory for '" + name + "' returned null");
        }
        BeanData beanData = beanDataMap.get(name);
        if (beanData == null) {
            Class<?> targetClass = target.getClass();
            if (targetClass.isRecord()) {
                if (targetClass.getInterfaces().length > 0) {
                    throw new IllegalArgumentException(
                        "Cannot create a proxy for '%s': Proxied Thread Scope does not support records implementing interfaces"
                            .formatted(targetClass.getName())
                    );
                }
                return target;
            }
            beanData = createBeanData(target);
            beanDataMap.put(name, beanData);
        } else if (beanData.isStale()) {
            beanData.useNewTarget(target);
        }
        return beanData.proxyBean();
    }

    @Override
    @Nullable public Object remove(@NotNull String name) {
        Map<String, BeanData> scope = this.threadScope.get();
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
        Map<String, BeanData> beanDataMap = threadScope.get();
        for (BeanData beanData : beanDataMap.values()) {
            beanData.markAsStale();
        }
    }

    private static BeanData createBeanData(Object target) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(target);
        StaleBeanCheckAdvice advice = new StaleBeanCheckAdvice();
        proxyFactory.addAdvice(advice);
        return new BeanData(proxyFactory, advice, proxyFactory.getProxy());
    }

    private record BeanData(ProxyFactory proxyFactory, StaleBeanCheckAdvice staleAdvice, Object proxyBean) {
        public void useNewTarget(Object target) {
            proxyFactory.setTarget(target);
            staleAdvice().stale(false);
        }
        public void markAsStale() {
            staleAdvice().stale(true);
        }
        public boolean isStale() {
            return staleAdvice().stale();
        }
    }

    @Getter
    @Setter
    private static final class StaleBeanCheckAdvice implements MethodBeforeAdvice {

        private boolean stale = false;

        @Override
        public void before(Method method, @Nullable Object[] args, @Nullable Object target) {
            if (stale) {
                throw new IllegalStateException(
                    "This instance of '%s' is stale. That means that the same instance has been used in more than one test method."
                        .formatted(castNonNull(target).getClass().getName())
                );
            }
        }
    }
}
