package com.theodo.springblueprint.transverse.architecture.domain;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.theodo.springblueprint.common.domain.events.Event;
import com.theodo.springblueprint.common.domain.exceptions.AbstractDomainException;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@UnitTest
@AnalyzeClasses(packages = BASE_PACKAGE_NAME, importOptions = { ImportOption.DoNotIncludeTests.class })
public class NamingConventionRulesUnitTests {

    @ArchTest
    static final ArchRule domain_events_should_be_in_events_package_and_implement_Event_interface = classes()
        .that()
        .areNotInterfaces()
        .and()
        .resideInAPackage("..domain..")
        .and()
        .haveSimpleNameEndingWith("Event")
        .should()
        .resideInAPackage("..domain.events")
        .andShould()
        .implement(Event.class);

    @ArchTest
    static final ArchRule classes_in_events_package_should_be_suffixed_with_Event = classes()
        .that()
        .resideInAPackage("..domain.events")
        .and()
        .areTopLevelClasses()
        .should()
        .haveSimpleNameEndingWith("Event");

    @ArchTest
    static final ArchRule domain_exceptions_should_inherit_from_AbstractDomainException = classes()
        .that()
        .areAssignableTo(Exception.class)
        .should()
        .beAssignableTo(AbstractDomainException.class);

    @ArchTest
    static final ArchRule ports_should_be_suffixed_with_Port = classes()
        .that()
        .resideInAPackage("..ports..")
        .should()
        .haveSimpleNameEndingWith("Port");

    @ArchTest
    static final ArchRule usecases_should_be_two_levels_under_usecases_package = classes()
        .that()
        .haveSimpleNameEndingWith("UseCase")
        .should()
        .resideInAPackage("..usecases.(*)");

    @ArchTest
    static final ArchRule usecases_packages_should_be_two_levels_under_domain_package = classes()
        .that()
        .resideInAPackage("..usecases..")
        .should()
        .resideInAPackage("..domain.usecases.(*)");

    @ArchTest
    // Challengeable
    static final ArchRule classes_in_usecases_package_should_have_specific_suffixes = classes()
        .that()
        .resideInAPackage("..usecases..")
        .and()
        .areTopLevelClasses()
        .and()
        .haveSimpleNameNotEndingWith("Builder")
        .and()
        .haveSimpleNameNotEndingWith("Builders")
        .should()
        .haveNameMatching(".*(?:UseCase|Command|Query)$");
}
