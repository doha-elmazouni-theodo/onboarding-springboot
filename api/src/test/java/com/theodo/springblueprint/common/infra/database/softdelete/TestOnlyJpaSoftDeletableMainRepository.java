package com.theodo.springblueprint.common.infra.database.softdelete;

import java.util.UUID;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface TestOnlyJpaSoftDeletableMainRepository extends JpaRepository<TestOnlySoftDeletableMainDbEntity, UUID> {
    @Query(value = "SELECT * FROM " + TestOnlySoftDeletableMainDbEntity.TABLE_NAME, nativeQuery = true)
    ImmutableList<TestOnlySoftDeletableMainDbEntity> findAllBypassRestriction();
}
