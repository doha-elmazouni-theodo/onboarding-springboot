package com.theodo.springblueprint.transverse.architecture;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;
import static com.tngtech.archunit.lang.conditions.ArchConditions.callMethod;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;

@UnitTest
@AnalyzeClasses(packages = BASE_PACKAGE_NAME)
public class DesignRulesUnitTests {

    @ArchTest
    static final ArchRule current_time_retrieving_methods_should_use_a_clock_instance_for_testability = noClasses()
        .should(callMethod(Instant.class, "now"))
        .orShould(callMethod(OffsetDateTime.class, "now"))
        .orShould(callMethod(ZonedDateTime.class, "now"))
        .orShould(callMethod(LocalDateTime.class, "now"))
        .orShould(callMethod(LocalDate.class, "now"))
        .orShould(callMethod(LocalTime.class, "now"))
        .orShould(callMethod(OffsetTime.class, "now"))
        .orShould(callMethod(System.class, "currentTimeMillis"))
        .orShould(callMethod(System.class, "nanoTime"))
        .because("You must use TimeProviderPort for testability");

    @ArchTest
    static final ArchRule fields_should_not_be_autowired = noFields()
        .should()
        .beAnnotatedWith(Autowired.class)
        .because("Field injection is discouraged. Use constructor injection instead");
}
