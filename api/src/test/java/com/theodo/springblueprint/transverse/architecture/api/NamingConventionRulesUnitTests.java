package com.theodo.springblueprint.transverse.architecture.api;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

@UnitTest
@AnalyzeClasses(packages = BASE_PACKAGE_NAME, importOptions = { ImportOption.DoNotIncludeTests.class })
public class NamingConventionRulesUnitTests {

    @ArchTest
    static final ArchRule RestEndpoint_annotation_should_be_used_on_classes_suffixed_with_Endpoint = classes()
        .that()
        .areAnnotatedWith(RestController.class)
        .should()
        .haveSimpleNameEndingWith("Endpoint");

    @ArchTest
    static final ArchRule classes_suffixed_with_Endpoint_should_be_annotated_with_RestEndpoint = classes()
        .that()
        .haveSimpleNameEndingWith("Endpoint")
        .should()
        .beAnnotatedWith(RestController.class);

    @ArchTest
    static final ArchRule Endpoints_should_be_two_levels_under_endpoints_package = classes()
        .that()
        .haveSimpleNameEndingWith("Endpoint")
        .should()
        .resideInAPackage("..endpoints.(*)");

    @ArchTest
    static final ArchRule endpoints_packages_should_be_two_levels_under_api_package = classes()
        .that()
        .resideInAPackage("..endpoints..")
        .should()
        .resideInAPackage("..api.endpoints.(*)");

    @ArchTest
    // Challengeable
    static final ArchRule classes_in_endpoints_package_should_have_specific_suffixes = classes()
        .that()
        .resideInAPackage("..endpoints..")
        .and()
        .areTopLevelClasses()
        .should()
        .haveNameMatching(".*(?:Endpoint|EndpointRequest|EndpointResponse)$");

    @ArchTest
    static final ArchRule methods_annotated_with_Scheduled_must_be_declared_in_classes_under_schedules_package = methods()
        .that()
        .areAnnotatedWith(Scheduled.class)
        .should()
        .beDeclaredInClassesThat()
        .resideInAPackage("..api.schedules.(*)");
}
