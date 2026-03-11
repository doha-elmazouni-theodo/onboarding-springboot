package com.theodo.springblueprint.transverse.architecture.infra;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

import com.theodo.springblueprint.Application;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@UnitTest
@AnalyzeClasses(packages = Application.BASE_PACKAGE_NAME, importOptions = { ImportOption.DoNotIncludeTests.class })
public class JpaRulesUnitTests {

    @ArchTest
    private final ArchRule entity_and_embeddable_attributes_should_be_annotated_with_nullability = fields()
        .that()
        .haveRawType(assignableTo(Object.class))
        .and()
        .areDeclaredInClassesThat()
        .areAnnotatedWith(Entity.class)
        .or()
        .areAnnotatedWith(Embeddable.class)
        .should()
        .beAnnotatedWith(Nullable.class)
        .orShould()
        .beAnnotatedWith(NotNull.class);

    @ArchTest
    private final ArchRule relationship_collection_attributes_must_be_notnull = fields()
        .that()
        .haveRawType(assignableTo(Iterable.class))
        .and()
        .areAnnotatedWith(OneToMany.class)
        .or()
        .areAnnotatedWith(ManyToMany.class)
        .and()
        .areDeclaredInClassesThat()
        .areAnnotatedWith(Entity.class)
        .or()
        .areAnnotatedWith(Embeddable.class)
        .should()
        .beAnnotatedWith(NotNull.class);

    @ArchTest
    private final ArchRule fields_annotated_with_GeneratedValue_should_be_annotated_with_nullable = fields()
        .that()
        .haveRawType(assignableTo(Object.class))
        .and()
        .areAnnotatedWith(GeneratedValue.class)
        .should()
        .beAnnotatedWith(Nullable.class);

    @ArchTest
    private final ArchRule classes_annotated_with_Entity_should_have_DbEntity_suffix = classes()
        .that()
        .areAnnotatedWith(Entity.class)
        .should()
        .haveSimpleNameEndingWith("DbEntity");

    @ArchTest
    private final ArchRule classes_with_DbEntity_suffix_should_be_annotated_with_Entity = classes()
        .that()
        .haveSimpleNameEndingWith("DbEntity")
        .should()
        .beAnnotatedWith(Entity.class);

    @ArchTest
    private final ArchRule classes_annotated_with_Embeddable_should_have_EmbeddableEntity_suffix = classes()
        .that()
        .areAnnotatedWith(Embeddable.class)
        .should()
        .haveSimpleNameEndingWith("EmbeddableEntity")
        .allowEmptyShould(true);

    @ArchTest
    private final ArchRule classes_with_EmbeddableEntity_suffix_should_be_annotated_with_Embeddable = classes()
        .that()
        .haveSimpleNameEndingWith("EmbeddableEntity")
        .should()
        .beAnnotatedWith(Embeddable.class)
        .allowEmptyShould(true);
}
