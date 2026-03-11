package com.theodo.springblueprint.features.authentication.domain.events;

import com.theodo.springblueprint.common.domain.events.Event;
import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;

public record UserLoggedInEvent(UserPrincipal userPrincipal, Class<?> sourceType) implements Event {
    @Override
    public String toString() {
        return "Login attempt successful for user with id: %s".formatted(userPrincipal.id());
    }
}
