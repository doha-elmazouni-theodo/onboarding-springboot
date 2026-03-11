package com.theodo.springblueprint.common.infra.mappers;

import com.theodo.springblueprint.common.domain.valueobjects.Role;

public class RoleMapper extends AbstractDefaultEnumMapper<Role> {

    public static final RoleMapper INSTANCE = new RoleMapper();

    public RoleMapper() {
        super(Role.class);
    }
}
