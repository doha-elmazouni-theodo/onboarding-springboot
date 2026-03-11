package com.theodo.springblueprint.common.infra.database.entities;

import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.infra.mappers.RoleMapper;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles")
public class RoleDbEntity {

    @Id
    @Column(name = "id")
    @Nullable private Integer id;

    @Column(name = "role_name", unique = true)
    @NotNull private String roleName;

    public static RoleDbEntity newFromRole(Role role) {
        RoleDbEntity roleDbEntity = new RoleDbEntity();
        roleDbEntity.id(role.id());
        roleDbEntity.roleName(RoleMapper.INSTANCE.fromValueObject(role));
        return roleDbEntity;
    }

    public Role toRole() {
        return RoleMapper.INSTANCE.toValueObject(roleName);
    }
}
