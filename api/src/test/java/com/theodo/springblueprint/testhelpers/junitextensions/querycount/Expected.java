package com.theodo.springblueprint.testhelpers.junitextensions.querycount;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expected {
    String sutActMethod();

    String testMethod();

    int count();
}
