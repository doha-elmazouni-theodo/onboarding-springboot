package com.theodo.springblueprint.common.infra.database.logging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.NoArgsConstructor;

@Entity
@Table(name = TestOnlyCommentedDbEntity.TABLE_NAME)
@NoArgsConstructor
public class TestOnlyCommentedDbEntity {

    static final String TABLE_NAME = "testonly_commented_entities";

    @Id
    private final UUID id = UUID.randomUUID();

    @Column(name = "name")
    private String name;

    TestOnlyCommentedDbEntity(String name) {
        this.name = name;
    }

    void setName(String name) {
        this.name = name;
    }

}
