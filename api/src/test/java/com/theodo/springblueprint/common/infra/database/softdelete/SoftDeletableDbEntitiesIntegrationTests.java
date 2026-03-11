package com.theodo.springblueprint.common.infra.database.softdelete;

import static com.theodo.springblueprint.common.infra.database.softdelete.TestOnlySoftDeletableMainDbEntity.TABLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatObject;

import com.theodo.springblueprint.testhelpers.annotations.IncludeTestOnlyDbTypes;
import com.theodo.springblueprint.testhelpers.annotations.SetupDatabase;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@SetupDatabase
@DataJpaTest
@Transactional
@IncludeTestOnlyDbTypes
@RequiredArgsConstructor
class SoftDeletableDbEntitiesIntegrationTests {

    private final TestOnlyJpaSoftDeletableMainRepository jpaSoftDeletableTestRepository;
    private final TestOnlyJpaSoftDeletableDependentRepository jpaDependentEntityTestRepository;
    private final EntityManager entityManager;

    @BeforeAll
    static void createIndex(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(
                """
                CREATE UNIQUE INDEX testonly_index_name
                    ON %s(value)
                    WHERE deleted_at IS NULL
                """.formatted(TABLE_NAME)
            );
        }
    }

    @Test
    void soft_deleting_then_creating_a_new_entity_does_not_throw_when_using_jpa_repositories() {
        TestOnlySoftDeletableMainDbEntity entity = insertNewEntityWithValueEqualsFive();
        jpaSoftDeletableTestRepository.delete(entity);

        // Act
        var codeAssertion = assertThatCode(this::insertNewEntityWithValueEqualsFive);

        codeAssertion
            .doesNotThrowAnyException();

        assertExistingMainDbEntities();
    }

    @Test
    void soft_deleting_then_creating_a_new_entity_does_not_throw_when_using_entity_manager() {
        TestOnlySoftDeletableMainDbEntity entity = new TestOnlySoftDeletableMainDbEntity(5);
        entityManager.persist(entity);
        entityManager.remove(entity);

        // Act
        var codeAssertion = assertThatCode(this::insertNewEntityWithValueEqualsFive);

        codeAssertion
            .doesNotThrowAnyException();

        assertExistingMainDbEntities();
    }

    @Test
    void soft_deleting_in_cascade_then_creating_a_new_entity_does_not_throw_when_using_jpa_repositories() {
        // Create mainEntity
        TestOnlySoftDeletableMainDbEntity mainEntity = insertNewEntityWithValueEqualsFive();

        // Create then delete dependentEntity referencing mainEntity
        TestOnlySoftDeletableDependentDbEntity dependentEntity = new TestOnlySoftDeletableDependentDbEntity(mainEntity);
        jpaDependentEntityTestRepository.saveAndFlush(dependentEntity);
        jpaDependentEntityTestRepository.delete(dependentEntity);

        // Act
        var codeAssertion = assertThatCode(this::insertNewEntityWithValueEqualsFive);

        codeAssertion
            .doesNotThrowAnyException();

        assertExistingMainDbEntities();
        assertExistingDependentDbEntities();
    }

    @Test
    void soft_deleting_in_cascade_then_creating_a_new_entity_does_not_throw_when_using_entity_manager() {
        // Create mainEntity
        TestOnlySoftDeletableMainDbEntity mainEntity = new TestOnlySoftDeletableMainDbEntity(5);
        entityManager.persist(mainEntity);

        // Create then delete dependentEntity referencing mainEntity
        TestOnlySoftDeletableDependentDbEntity dependentEntity = new TestOnlySoftDeletableDependentDbEntity(mainEntity);
        entityManager.persist(dependentEntity);
        entityManager.flush();
        entityManager.remove(dependentEntity);

        // Act
        var codeAssertion = assertThatCode(this::insertNewEntityWithValueEqualsFive);

        codeAssertion
            .doesNotThrowAnyException();

        assertExistingMainDbEntities();
        assertExistingDependentDbEntities();
    }

    private TestOnlySoftDeletableMainDbEntity insertNewEntityWithValueEqualsFive() {
        TestOnlySoftDeletableMainDbEntity entity = new TestOnlySoftDeletableMainDbEntity(5);
        jpaSoftDeletableTestRepository.saveAndFlush(entity);
        return entity;
    }

    private void assertExistingDependentDbEntities() {
        ImmutableList<TestOnlySoftDeletableDependentDbEntity> dependentEntities = jpaDependentEntityTestRepository
            .findAllBypassRestriction();
        assertThat(dependentEntities)
            .hasSize(1)
            .allSatisfy(
                dependent -> assertThat(dependent)
                    .extracting(TestOnlySoftDeletableDependentDbEntity::deletedAt)
                    .isNotNull()
            );
    }

    private void assertExistingMainDbEntities() {
        ImmutableList<TestOnlySoftDeletableMainDbEntity> mainEntities = jpaSoftDeletableTestRepository
            .findAllBypassRestriction();
        assertThat(mainEntities)
            .hasSize(2)
            .extracting(TestOnlySoftDeletableMainDbEntity::deletedAt)
            .satisfiesExactlyInAnyOrder(
                deletedAt -> assertThatObject(deletedAt).isNull(),
                deletedAt -> assertThatObject(deletedAt).isNotNull()
            );
    }
}
