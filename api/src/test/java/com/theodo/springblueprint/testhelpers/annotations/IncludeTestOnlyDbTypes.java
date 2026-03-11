package com.theodo.springblueprint.testhelpers.annotations;

import static com.theodo.springblueprint.testhelpers.annotations.IncludeTestOnlyDbTypes.PROPERTY_NAME;

import java.lang.annotation.*;
import org.springframework.test.context.TestPropertySource;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource(properties = { PROPERTY_NAME + "=true" })
public @interface IncludeTestOnlyDbTypes {
    String PROPERTY_NAME = "spring.jpa.include-test-only-db-entities";
}
