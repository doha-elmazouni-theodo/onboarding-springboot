package com.theodo.springblueprint.common.infra.database.logging;

import com.theodo.springblueprint.common.utils.collections.Immutable;
import java.util.UUID;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestOnlyCommentedRepository extends JpaRepository<TestOnlyCommentedDbEntity, UUID> {
    default ImmutableList<TestOnlyCommentedDbEntity> selectAll() {
        return Immutable.list.ofAll(findAll());
    }

    default TestOnlyCommentedDbEntity saveNewEntity() {
        return saveAndFlush(new TestOnlyCommentedDbEntity("first"));
    }

    default void updateNameField(TestOnlyCommentedDbEntity entity) {
        entity.setName("second");
        saveAndFlush(entity);
    }

    default void deleteAndFlush(TestOnlyCommentedDbEntity entity) {
        delete(entity);
        flush();
    }
}
