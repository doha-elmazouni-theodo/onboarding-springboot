package com.theodo.springblueprint.common.infra.database.softdelete;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = TestOnlySoftDeletableDependentDbEntity.TABLE_NAME)
@NoArgsConstructor
@Getter
@Setter
@SQLDelete(sql = "UPDATE " + TestOnlySoftDeletableDependentDbEntity.TABLE_NAME + " SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class TestOnlySoftDeletableDependentDbEntity {

    static final String TABLE_NAME = "testonly_dependent_entity";

    @Id
    @NotNull private UUID id = UUID.randomUUID();

    @OneToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "parent_id")
    @NotNull private TestOnlySoftDeletableMainDbEntity parent;

    @Column(name = "deleted_at")
    @Nullable private Instant deletedAt;

    public TestOnlySoftDeletableDependentDbEntity(TestOnlySoftDeletableMainDbEntity parent) {
        this.parent = parent;
    }
}
