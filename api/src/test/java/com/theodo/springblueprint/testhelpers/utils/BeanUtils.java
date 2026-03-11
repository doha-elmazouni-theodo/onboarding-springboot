package com.theodo.springblueprint.testhelpers.utils;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.MethodMetadata;

public interface BeanUtils {
    @Nullable static String getBeanClassName(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
            MethodMetadata factoryMethodMetadata = annotatedBeanDefinition.getFactoryMethodMetadata();
            if (factoryMethodMetadata != null) {
                return factoryMethodMetadata.getReturnTypeName();
            }
        }
        return beanDefinition.getBeanClassName();
    }
}
