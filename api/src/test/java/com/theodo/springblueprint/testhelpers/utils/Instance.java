package com.theodo.springblueprint.testhelpers.utils;

import com.theodo.springblueprint.common.utils.collections.Immutable;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

public class Instance {

    private static final String SCOPE_NAME = "perCall";
    private static final ClearableSimpleThreadScope SCOPE = new ClearableSimpleThreadScope();
    private static final MutableMap<ImmutableSet<Class<?>>, Data> CONTEXT_MAP = new ConcurrentHashMap<>();

    public static <T> T create(Class<T> clazz) {
        Data data = CONTEXT_MAP.computeIfAbsent(Immutable.set.of(clazz), Instance::createContext);
        return getNewBean(data.context(), clazz);
    }

    public static BeanBag createBeanBag(Class<?>... classes) {
        Data data = CONTEXT_MAP.computeIfAbsent(Immutable.set.of(classes), Instance::createContext);
        return getNewBeans(data);
    }

    public static BeanBag createBeanBag(ImmutableSet<Class<?>> classes) {
        Data data = CONTEXT_MAP.computeIfAbsent(classes, Instance::createContext);
        return getNewBeans(data);
    }

    private static BeanBag getNewBeans(Data data) {
        ImmutableSet<Object> beans = Immutable.collectSet(data.newBeans(), data.context()::getBean);
        SCOPE.clear();
        return new BeanBag(beans);
    }

    private static <T> T getNewBean(AnnotationConfigApplicationContext ctx, Class<T> clazz) {
        T bean = ctx.getBean(clazz);
        SCOPE.clear();
        return bean;
    }

    private static Data createContext(final ImmutableSet<Class<?>> classes) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getBeanFactory().registerScope(SCOPE_NAME, SCOPE);
        ImmutableSet<String> initialBeanNames = Immutable.collectSet(ctx.getBeanDefinitionNames(), n -> n);
        registerClassesTransitively(ctx, classes);
        ctx.refresh();
        removeRedundantBeans(ctx, initialBeanNames);
        ImmutableSet<String> newBeans = Immutable.collectSet(ctx.getBeanDefinitionNames(), n -> n).difference(
            initialBeanNames
        );
        return new Data(ctx, newBeans);
    }

    private static void registerClassesTransitively(
        AnnotationConfigApplicationContext ctx,
        Iterable<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            ctx.registerBean(clazz.getName(), clazz, bd -> bd.setScope(SCOPE_NAME));
            Import importAnnotation = clazz.getAnnotation(Import.class);
            if (importAnnotation != null) {
                registerClassesTransitively(ctx, Arrays.stream(importAnnotation.value())::iterator);
            }
        }
    }

    private static void removeRedundantBeans(
        AnnotationConfigApplicationContext ctx,
        ImmutableSet<String> initialBeanNames) {
        DefaultListableBeanFactory beanFactory = ctx.getDefaultListableBeanFactory();
        for (String bean : getBeansToRemove(initialBeanNames, beanFactory)) {
            beanFactory.removeBeanDefinition(bean);
        }
        beanFactory.freezeConfiguration();
    }

    private static ImmutableSet<String> getBeansToRemove(
        ImmutableSet<String> initialBeanNames,
        DefaultListableBeanFactory beanFactory) {
        return Arrays.stream(beanFactory.getBeanDefinitionNames())
            .filter(name -> !initialBeanNames.contains(name))
            .collect(Collectors.groupingBy(beanName -> getBeanClassName(beanName, beanFactory)))
            .values().stream()
            .filter(beanNames -> beanNames.size() > 1)
            .flatMap(beanNames -> {
                String longestBeanName = beanNames.stream()
                    .max(Comparator.comparingInt(String::length))
                    .orElseThrow();
                return beanNames.stream().filter(name -> !name.equals(longestBeanName));
            })
            .collect(Collectors2.toImmutableSet());
    }

    private static String getBeanClassName(String beanName, DefaultListableBeanFactory beanFactory) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        return castNonNull(BeanUtils.getBeanClassName(beanDefinition));
    }

    private record Data(AnnotationConfigApplicationContext context, ImmutableSet<String> newBeans) {
    }
}
