package com.theodo.springblueprint.common.infra.database.softdelete;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = TestOnlySoftDeletableMainDbEntity.TABLE_NAME)
@NoArgsConstructor
@Getter
@Setter
@SQLDelete(sql = "UPDATE " + TestOnlySoftDeletableMainDbEntity.TABLE_NAME + " SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class TestOnlySoftDeletableMainDbEntity {

    static final String TABLE_NAME = "testonly_main_entity";

    @Id
    @NotNull private UUID id = UUID.randomUUID();

    @Column(name = "value")
    private int value;

    @Column(name = "deleted_at")
    @Nullable private Instant deletedAt;

    public TestOnlySoftDeletableMainDbEntity(int value) {
        this.value = value;
    }
}
