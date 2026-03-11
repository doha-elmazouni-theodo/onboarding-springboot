package com.theodo.springblueprint.transverse.architecture;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.be;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.theodo.springblueprint.testhelpers.annotations.IntegrationTest;
import com.theodo.springblueprint.testhelpers.annotations.SetupDatabase;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.theodo.springblueprint.testhelpers.baseclasses.AbstractApplicationTests;
import com.theodo.springblueprint.testhelpers.baseclasses.exceptionhandling.BaseExceptionHandlerIntegrationTests;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.elements.GivenClassesConjunction;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;

@UnitTest
@AnalyzeClasses(packages = BASE_PACKAGE_NAME, importOptions = { ImportOption.OnlyIncludeTests.class })
public class TestsNamingConventionRulesUnitTests {

    private static final GivenClassesConjunction testClasses = classes()
        .that(
            be(
                DescribedPredicate.describe(
                    "a subclass of a class with test methods",
                    TestsNamingConventionRulesUnitTests::subclassOfTestClass
                )
            )
        )
        .or()
        .containAnyMethodsThat(annotatedWith(Test.class))
        .and()
        .doNotHaveModifier(JavaModifier.ABSTRACT)
        .and()
        .areNotAnnotatedWith(Nested.class);

    private static final GivenClassesConjunction integrationTestsClasses = testClasses
        // TODO: add check for @ExtendWith(SpringExtension.class)
        .and()
        .areAnnotatedWith(WebMvcTest.class)
        .or()
        .areAnnotatedWith(DataJpaTest.class)
        .or()
        .areAnnotatedWith(SetupDatabase.class)
        .or()
        .areAnnotatedWith(IntegrationTest.class)
        .or()
        .areAssignableTo(BaseExceptionHandlerIntegrationTests.class)
        .and()
        .areNotAnnotatedWith(SpringBootTest.class)
        .and()
        .areNotAssignableTo(AbstractApplicationTests.class)
        .and()
        .haveSimpleNameNotEndingWith("Sut");

    private static final GivenClassesConjunction unitTestsClasses = testClasses
        .and()
        .areNotAnnotatedWith(WebMvcTest.class)
        .and()
        .areNotAnnotatedWith(DataJpaTest.class)
        .and()
        .areNotAnnotatedWith(SetupDatabase.class)
        .and()
        .areNotAnnotatedWith(SpringBootTest.class)
        .and()
        .areNotAnnotatedWith(Import.class)
        .and()
        .areNotAnnotatedWith(IntegrationTest.class)
        .and()
        .areNotAssignableTo(BaseExceptionHandlerIntegrationTests.class)
        .and()
        .areNotAssignableTo(AbstractApplicationTests.class);

    @ArchTest
    static final ArchRule test_classes_should_have_suffix_UnitTests_or_IntegrationTests = testClasses
        .should()
        .haveNameMatching(".*(UnitTests|IntegrationTests|ApplicationTests)$");

    @ArchTest
    static final ArchRule unitTest_classes_should_have_suffix_UnitTests = unitTestsClasses
        .should()
        .haveSimpleNameEndingWith("UnitTests");

    @ArchTest
    static final ArchRule integrationTest_classes_should_have_suffix_IntegrationTests = integrationTestsClasses
        .should()
        .haveSimpleNameEndingWith("IntegrationTests");

    @ArchTest
    static final ArchRule test_classes_that_have_suffix_UnitTest_Should_be_annotated_with_UnitTests = testClasses
        .and()
        .haveSimpleNameEndingWith("UnitTests")
        .should()
        .beAnnotatedWith(UnitTest.class);

    @ArchTest
    static final ArchRule all_tests_in_domain_must_be_unit_tests = testClasses
        .and()
        .resideInAnyPackage("..common.domain..", "..features..domain..")
        .should()
        .haveSimpleNameEndingWith("UnitTests");

    @ArchTest
    static final ArchRule abstract_classes_with_test_methods_should_be_suffixed_with_ContractTests = classes()
        .that()
        .containAnyMethodsThat(annotatedWith(Test.class))
        .and()
        .haveModifier(JavaModifier.ABSTRACT)
        .and()
        .areNotAssignableTo(BaseExceptionHandlerIntegrationTests.class)
        .should()
        .haveSimpleNameEndingWith("ContractTests");

    @ArchTest
    static final ArchRule archUnit_tests_should_end_with_RulesUnitTests = classes()
        .that()
        .containAnyMembersThat(annotatedWith(ArchTest.class))
        .should()
        .haveSimpleNameEndingWith("RulesUnitTests");

    // https://www.sivalabs.in/quirks-of-spring-testconfiguration/
    @ArchTest
    static final ArchRule beans_defined_in_the_testconfig_of_an_application_test_class_must_be_primary = methods()
        .that()
        .areAnnotatedWith(Bean.class)
        .and(
            are(
                DescribedPredicate.describe(
                    "defined in the TestConfiguration subclass of a @SpringBootTest-annotated class",
                    method -> method.getOwner().isMetaAnnotatedWith(TestConfiguration.class) &&
                        method
                            .getOwner()
                            .getEnclosingClass()
                            .map(
                                javaClass -> javaClass.isMetaAnnotatedWith(SpringBootTest.class) ||
                                    javaClass
                                        .getAllRawSuperclasses()
                                        .stream()
                                        .anyMatch(c -> c.toErasure().isMetaAnnotatedWith(SpringBootTest.class))
                            )
                            .orElse(false)
                )
            )
        )
        .should()
        .beAnnotatedWith(Primary.class);

    @ArchTest
    static final ArchRule should_use_real_authentication_instead_of_WithMockUser = methods()
        .that()
        .areMetaAnnotatedWith(Test.class)
        .should()
        .notBeMetaAnnotatedWith(WithMockUser.class)
        .because("you should use getAccessTokenCookie() from BaseWebMvcIntegrationTests instead");

    static boolean subclassOfTestClass(JavaClass javaClass) {
        Optional<JavaClass> superclass = javaClass.getSuperclass().map(JavaType::toErasure);
        return superclass
            .map(aClass -> aClass.getAllMethods().stream().anyMatch(m -> m.isAnnotatedWith(Test.class)))
            .orElse(false);
    }
}
