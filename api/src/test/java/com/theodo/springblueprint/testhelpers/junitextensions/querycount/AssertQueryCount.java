package com.theodo.springblueprint.testhelpers.junitextensions.querycount;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(QueryCountingExtension.class)
@Import(QueryCountingExtension.SutMethodWrapBeanPostProcessor.class)
public @interface AssertQueryCount {
    Class<?> sutClass();

    Expected[] value();
}
