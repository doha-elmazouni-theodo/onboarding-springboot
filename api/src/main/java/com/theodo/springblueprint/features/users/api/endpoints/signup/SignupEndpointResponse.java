package com.theodo.springblueprint.features.users.api.endpoints.signup;

import com.theodo.springblueprint.common.infra.mappers.RoleMapper;
import com.theodo.springblueprint.features.users.domain.entities.User;
import org.eclipse.collections.api.set.ImmutableSet;

public record SignupEndpointResponse(String id, String name, String username, ImmutableSet<String> roles) {
    public static SignupEndpointResponse from(User user) {
        ImmutableSet<String> userRoles = RoleMapper.INSTANCE.fromValueObjects(user.roles());
        return new SignupEndpointResponse(user.id().toString(), user.name(), user.username().value(), userRoles);
    }
}
