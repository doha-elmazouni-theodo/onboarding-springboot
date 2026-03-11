package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;
import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.common.utils.collections.Mutable;
import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import com.theodo.springblueprint.features.users.domain.entities.User;
import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.collections.api.map.MutableMap;

@Getter(AccessLevel.PACKAGE)
public class FakeDatabase {

    private final MutableMap<Username, UserWithPassword> userTable = Mutable.map.empty();
    private final MutableMap<RefreshToken, UserSession> sessionTable = Mutable.map.empty();

    record UserWithPassword(User user, EncodedPassword encodedPassword) {
    }
}
