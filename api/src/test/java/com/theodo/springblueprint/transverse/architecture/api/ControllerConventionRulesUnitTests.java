package com.theodo.springblueprint.transverse.architecture.api;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;
import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.theodo.springblueprint.common.api.security.WebSecurityConfiguration;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@UnitTest
@AnalyzeClasses(packages = BASE_PACKAGE_NAME, importOptions = { ImportOption.DoNotIncludeTests.class })
public class ControllerConventionRulesUnitTests {

    @ArchTest
    static final ArchRule controllers_should_not_have_class_level_request_mapping = classes()
        .that()
        .areMetaAnnotatedWith(Controller.class)
        .should()
        .notBeAnnotatedWith(RequestMapping.class)
        .because("request mappings must be defined at method level only");

    @ArchTest
    static final ArchRule mapping_methods_should_require_authorization = methods()
        .that(areAnnotatedWithSpringMapping())
        .and(not(arePublicEndpoints()))
        .and(not(areInErrorController()))
        .should()
        .beAnnotatedWith(PreAuthorize.class)
        .because("authorization must be explicit on every route");

    private static DescribedPredicate<JavaMethod> areAnnotatedWithSpringMapping() {
        return describe(
            "annotated with a Spring Mapping",
            method -> method.isMetaAnnotatedWith(RequestMapping.class)
        );
    }

    private static DescribedPredicate<JavaMethod> areInErrorController() {
        return describe(
            "in an ErrorController",
            method -> method.getOwner().isAssignableTo(ErrorController.class)
        );
    }

    private static DescribedPredicate<JavaMethod> arePublicEndpoints() {
        return describe(
            "mapped under %s".formatted(WebSecurityConfiguration.PUBLIC_PATH_SEGMENT),
            method -> {
                List<String> paths = method.getAnnotations()
                    .stream()
                    .filter(annotation -> annotation.getRawType().isMetaAnnotatedWith(RequestMapping.class))
                    .flatMap(
                        annotation -> Stream.concat(
                            getPathValues(annotation, "value"),
                            getPathValues(annotation, "path")
                        )
                    )
                    .toList();

                return paths.isEmpty()
                    || paths.stream().allMatch(path -> path.contains(WebSecurityConfiguration.PUBLIC_PATH_SEGMENT));
            }
        );
    }

    private static Stream<String> getPathValues(JavaAnnotation<JavaMethod> annotation, String propertyName) {
        return annotation.get(propertyName)
            .filter(String[].class::isInstance)
            .map(String[].class::cast)
            .filter(paths -> paths.length > 0)
            .stream()
            .flatMap(Arrays::stream);
    }

}
