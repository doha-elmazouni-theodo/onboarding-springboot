package com.theodo.springblueprint.transverse.architecture;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SliceAssignment;
import com.tngtech.archunit.library.dependencies.SliceIdentifier;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import java.util.Arrays;

@UnitTest
@AnalyzeClasses(packages = BASE_PACKAGE_NAME, importOptions = { ImportOption.DoNotIncludeTests.class })
public class HexagonalArchitectureRulesUnitTests {

    @ArchTest
    static final ArchRule subdomains_should_depend_only_on_common_utils_and_common_domain_and_java_libraries_with_few_exceptions = classes()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .onlyDependOnClassesThat(
            resideInAnyPackage(
                "..domain..",
                "java..",
                "javax..",
                "jakarta..",
                "org.eclipse.collections.api..",
                "org.checkerframework.dataflow.qual",
                "org.checkerframework.checker.nullness.util",
                "lombok..",
                "..common.utils.."
            )
            // !! IMPORTANT !! Challenge any change to the list above with your TL and/or EM
        );

    @ArchTest
    static final ArchRule common_domain_should_depend_only_on_common_utils_and_java_libraries_with_few_exceptions = classes()
        .that()
        .resideInAPackage("..common.domain..")
        .should()
        .onlyDependOnClassesThat(
            resideInAnyPackage(
                "..common.domain..",
                "java..",
                "javax..",
                "jakarta..",
                "org.eclipse.collections.api..",
                "org.checkerframework.dataflow.qual",
                "org.checkerframework.checker.nullness.util",
                "lombok..",
                "..common.utils.."
            )
            // !! IMPORTANT !! Challenge any change to the list above with your TL and/or EM
        );

    @ArchTest
    static final ArchRule fields_should_use_port_type_instead_of_adapter = fields()
        .should()
        .haveRawType(
            DescribedPredicate.describe(
                "of the port interface instead of the adapter's",
                t -> t.isInterface() ||
                    t
                        .toErasure()
                        .getAllRawInterfaces()
                        .stream()
                        .noneMatch(i -> i.getFullName().contains(".domain.ports."))
            )
        );

    @ArchTest
    static final ArchRule common_utils_should_depend_only_on_and_java_libraries_with_few_exceptions = classes()
        .that()
        .resideInAPackage("..common.utils..")
        .should()
        .onlyDependOnClassesThat(
            resideInAnyPackage(
                "..common.utils..",
                "java..",
                "javax..",
                "jakarta..",
                "org.eclipse.collections.api..",
                "org.checkerframework.dataflow.qual",
                "org.checkerframework.checker.nullness.util",
                "lombok.."
            )
            // !! IMPORTANT !! Challenge any change to the list above with your TL and/or EM
        );

    private static final SliceAssignment subdomains = new SliceAssignment() {
        @Override
        public SliceIdentifier getIdentifierOf(JavaClass javaClass) {
            if (
                !javaClass.getPackageName().contains(".domain.") ||
                    javaClass.getPackageName().contains(".common.domain.")
            ) {
                return SliceIdentifier.ignore();
            }
            String[] split = javaClass.getPackageName().split("\\.");
            int domainIndex = Arrays.stream(split).toList().indexOf("domain");
            return SliceIdentifier.of(split[domainIndex - 1]);
        }

        // this will be part of the rule description if the test fails
        @Override
        public String getDescription() {
            return "hexagonal package structure";
        }
    };

    @ArchTest
    static final ArchRule subdomains_should_not_depend_on_each_other = SlicesRuleDefinition.slices()
        .assignedFrom(subdomains)
        .should()
        .notDependOnEachOther();
}
