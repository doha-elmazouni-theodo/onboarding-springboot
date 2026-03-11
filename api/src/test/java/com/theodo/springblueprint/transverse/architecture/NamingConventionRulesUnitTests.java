package com.theodo.springblueprint.transverse.architecture;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.theodo.springblueprint.common.domain.events.Event;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@UnitTest
@AnalyzeClasses(packages = BASE_PACKAGE_NAME, importOptions = { ImportOption.DoNotIncludeTests.class })
public class NamingConventionRulesUnitTests {

    @ArchTest
    static final ArchRule classes_that_implement_Event_interface_must_end_with_Event = classes()
        .that()
        .implement(Event.class)
        .should()
        .haveSimpleNameEndingWith("Event");

    @ArchTest
    static final ArchRule classes_extending_Exception_should_end_with_Exception = classes()
        .that()
        .areAssignableTo(Exception.class)
        .should()
        .haveSimpleNameEndingWith("Exception");

    @ArchTest
    static final ArchRule classes_ending_with_Exception_should_extend_Exception = classes()
        .that()
        .haveSimpleNameEndingWith("Exception")
        .should()
        .beAssignableTo(Exception.class);

    @ArchTest
    static final ArchRule classes_in_exceptions_package_should_be_suffixed_with_Exception = classes()
        .that()
        .resideInAPackage("..exceptions..")
        .should()
        .haveSimpleNameEndingWith("Exception");
}
