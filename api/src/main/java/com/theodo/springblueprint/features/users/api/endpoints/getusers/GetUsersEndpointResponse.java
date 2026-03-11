package com.theodo.springblueprint.features.users.api.endpoints.getusers;

import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.features.users.domain.entities.User;
import org.eclipse.collections.api.list.ImmutableList;

public record GetUsersEndpointResponse(ImmutableList<Item> users) {

    public record Item(String id, String name, String username) {
        private static Item from(User user) {
            return new Item(user.id().toString(), user.name(), user.username().value());
        }
    }

    public static GetUsersEndpointResponse from(final ImmutableList<User> users) {
        return new GetUsersEndpointResponse(Immutable.collectList(users, Item::from));
    }

}
