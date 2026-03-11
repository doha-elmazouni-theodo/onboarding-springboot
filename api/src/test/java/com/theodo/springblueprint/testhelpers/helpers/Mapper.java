package com.theodo.springblueprint.testhelpers.helpers;

import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;
import com.theodo.springblueprint.features.users.domain.entities.User;

public class Mapper {

    public static UserPrincipal toUserPrincipal(User user) {
        return new UserPrincipal(user.id(), user.username(), user.roles());
    }
}
