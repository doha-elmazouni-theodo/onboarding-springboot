package com.theodo.springblueprint.testhelpers.utils;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;

import com.theodo.springblueprint.common.utils.collections.Immutable;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.ImmutableList;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;

public final class ClassFinder {

    private static final int BASE_PACKAGE_DEPTH = BASE_PACKAGE_NAME.split("\\.").length;

    private ClassFinder() {
    }

    public static ImmutableList<Class<?>> findAllNonAbstractExceptions(String packageName) {
        return Immutable.list.fromStream(
            getTypesAssignableTo(Throwable.class, packageName).filter(
                c -> !isTestClass(c) && !Modifier.isPrivate(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers())
            )
        );
    }

    public static <T> Stream<Class<? extends T>> getTypesAssignableTo(Class<T> targetType, String packageName) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(
            new SimpleBeanDefinitionRegistry(),
            false
        );
        scanner.addIncludeFilter(new AssignableTypeFilter(targetType));
        Stream<?> classStream = scanner
            .findCandidateComponents(packageName)
            .stream()
            .map(ClassFinder::getClassOrNull)
            .filter(Objects::nonNull);
        return getClassStream(classStream);
    }

    @SuppressWarnings("nullness:type.arguments.not.inferred")
    private static <T> Stream<Class<? extends T>> getClassStream(Stream<?> classStream) {
        return classStream
            .map(clazz -> {
                // This cast is safe because we filtered with AssignableTypeFilter
                @SuppressWarnings("unchecked")
                Class<? extends T> typedClass = (Class<? extends T>) clazz;
                return typedClass;
            });
    }

    public static String getParentPackage(Class<?> clazz) {
        String currentPackageName = clazz.getPackageName();
        if (!currentPackageName.startsWith(BASE_PACKAGE_NAME)) {
            throw new IllegalArgumentException(
                "Current package '%s' is not under the base package '%s'".formatted(
                    currentPackageName,
                    BASE_PACKAGE_NAME
                )
            );
        }
        String[] currentPackageNameArray = currentPackageName.split("\\.");
        String packageUnderBasePackage = currentPackageNameArray[BASE_PACKAGE_DEPTH];
        return switch (packageUnderBasePackage) {
            case "common" -> BASE_PACKAGE_NAME + ".common";
            case "features" -> BASE_PACKAGE_NAME + ".features." + currentPackageNameArray[BASE_PACKAGE_DEPTH + 1];
            default -> throw new IllegalArgumentException(
                "the package '%s' just under the base package '%s' is unknown".formatted(
                    packageUnderBasePackage,
                    BASE_PACKAGE_NAME
                )
            );
        };
    }

    private static boolean isTestClass(Class<?> clazz) {
        URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
        return resource != null && resource.getPath().contains("/test-classes/");
    }

    @Nullable private static Class<?> getClassOrNull(BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName == null) {
            return null;
        }
        try {
            return Class.forName(beanClassName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
