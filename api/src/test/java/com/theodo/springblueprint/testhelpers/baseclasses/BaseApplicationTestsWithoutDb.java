package com.theodo.springblueprint.testhelpers.baseclasses;

import com.theodo.springblueprint.Application;
import com.theodo.springblueprint.testhelpers.utils.ClassFinder;
import java.lang.reflect.Field;
import java.util.Arrays;

import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.Repository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;

@TestPropertySource(
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
    }
)
@Import({ BaseApplicationTestsWithoutDb.BaseTestConfig.class })
public abstract class BaseApplicationTestsWithoutDb extends AbstractApplicationTests {

    private static final Class<?>[] TYPE_HIERARCHIES_TO_MOCK = {
        Repository.class,
        EntityManager.class,
    };

    protected BaseApplicationTestsWithoutDb() {
        super();
    }

    @TestConfiguration
    @SuppressWarnings("PMD.AvoidUncheckedExceptionsInSignatures") // Inheritance constraint
    static class BaseTestConfig implements BeanDefinitionRegistryPostProcessor {

        @Override
        public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry) throws BeansException {
            ClassFinder.getTypesAssignableTo(Object.class, Application.BASE_PACKAGE_NAME)
                .flatMap(c -> Arrays.stream(c.getDeclaredFields()))
                .map(Field::getType)
                .filter(BaseApplicationTestsWithoutDb::shouldBeMocked)
                .distinct()
                .forEach(clazz -> registerAsMock(registry, clazz));
        }

        @Override
        public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
            // No further processing required
        }

        private static void registerAsMock(BeanDefinitionRegistry registry, Class<?> clazz) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition();
            beanDefinition.setBeanClass(clazz);
            beanDefinition.setInstanceSupplier(() -> Mockito.mock(clazz));
            beanDefinition.setTargetType(clazz);
            beanDefinition.setRole(BeanDefinition.ROLE_APPLICATION);
            registry.registerBeanDefinition(StringUtils.uncapitalize(clazz.getSimpleName()), beanDefinition);
        }
    }

    private static boolean shouldBeMocked(Class<?> aClass) {
        return Arrays.stream(TYPE_HIERARCHIES_TO_MOCK).anyMatch(t -> t.isAssignableFrom(aClass));
    }
}
