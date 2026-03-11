package com.theodo.springblueprint.common.infra.database.softdelete;

import java.util.UUID;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface TestOnlyJpaSoftDeletableDependentRepository
    extends
        JpaRepository<TestOnlySoftDeletableDependentDbEntity, UUID> {
    @Query(value = "SELECT * FROM " + TestOnlySoftDeletableDependentDbEntity.TABLE_NAME, nativeQuery = true)
    ImmutableList<TestOnlySoftDeletableDependentDbEntity> findAllBypassRestriction();
}
