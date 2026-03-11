package com.theodo.springblueprint.transverse.architecture.api;

import static com.theodo.springblueprint.Application.BASE_PACKAGE_NAME;
import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.have;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Collection;
import org.eclipse.collections.api.list.ImmutableList;

@UnitTest
@AnalyzeClasses(packages = BASE_PACKAGE_NAME, importOptions = { ImportOption.DoNotIncludeTests.class })
public class RequestResponseConventionRulesUnitTests {

    private static final ImmutableList<String> allowedBuiltInPackages = Immutable.list.of("java.lang", "java.time");

    @ArchTest
    static final ArchRule checkBaseClasses = classes()
        .that()
        .resideInAPackage("..endpoints..")
        .and()
        .haveNameMatching(".*(?:EndpointRequest|EndpointResponse)(\\$.*)?$")
        .should(
            be(
                describe(
                    "a subclass of a built-in java type or an inner class type",
                    RequestResponseConventionRulesUnitTests::checkBaseType
                )
            )
        );

    @ArchTest
    static final ArchRule checkRequestsFieldTypes = fields()
        .that()
        .areDeclaredInClassesThat()
        .resideInAPackage("..endpoints..")
        .and()
        .areDeclaredInClassesThat()
        .haveNameMatching(".*(?:EndpointRequest)(\\$.*)?$")
        .should(
            have(
                describe(
                    "a non-primitive built-in java type or an inner class type",
                    field -> checkFieldType(field, false)
                )
            )
        );

    @ArchTest
    static final ArchRule checkResponsesFieldTypes = fields()
        .that()
        .areDeclaredInClassesThat()
        .resideInAPackage("..endpoints..")
        .and()
        .areDeclaredInClassesThat()
        .haveNameMatching(".*(?:EndpointResponse)(\\$.*)?$")
        .should(
            have(
                describe("a primitive, built-in java type or an inner class type", field -> checkFieldType(field, true))
            )
        );

    @ArchTest
    static final ArchRule checkRequestsFieldConstraints = fields()
        .that()
        .areDeclaredInClassesThat()
        .resideInAPackage("..endpoints..")
        .and()
        .areDeclaredInClassesThat()
        .haveNameMatching(".*(?:EndpointRequest)(\\$.*)?$")
        .and()
        .areNotStatic()
        .should(
            have(
                describe(
                    "a validation constraint annotation",
                    RequestResponseConventionRulesUnitTests::checkFieldConstraints
                )
            )
        );

    private static boolean checkFieldConstraints(JavaField field) {
        for (JavaAnnotation<JavaField> annotation : field.getAnnotations()) {
            if (
                annotation.getType().getName().startsWith("jakarta.validation.constraints.") ||
                    annotation.getType().getName().startsWith("jakarta.annotation.")
            ) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkBaseType(JavaClass type) {
        if (type.getSuperclass().isEmpty()) {
            return true;
        }
        for (JavaClass superType : type.getSuperclass().orElseThrow().getAllInvolvedRawTypes()) {
            if (!isTypeAllowed(superType, type, false)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFieldType(JavaField field, boolean allowPrimitives) {
        for (JavaClass type : field.getAllInvolvedRawTypes()) {
            if (!isTypeAllowed(type, field.getOwner(), allowPrimitives)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTypeAllowed(JavaClass type, JavaClass owner, boolean allowPrimitives) {
        if (type.isPrimitive()) {
            return allowPrimitives;
        }
        if (allowedBuiltInPackages.contains(type.getPackageName())) {
            return true;
        }
        if (type.getPackageName().startsWith("org.eclipse.collections.api.")) {
            return true;
        }
        if (type.isAssignableTo(Collection.class)) {
            return true;
        }
        if (type.isArray()) {
            return isTypeAllowed(type.getComponentType(), owner, allowPrimitives);
        }
        return type.getFullName().startsWith(owner.getFullName()); // inner class
    }
}
