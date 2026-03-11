package com.theodo.springblueprint.features.authentication.domain.usecases.helpers;

import static com.theodo.springblueprint.testhelpers.fixtures.SimpleFixture.aUserTokens;
import static com.theodo.springblueprint.testhelpers.helpers.Mapper.toUserPrincipal;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.features.authentication.domain.entities.TokenClaims;
import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;
import com.theodo.springblueprint.features.authentication.domain.ports.TokenClaimsCodecPort;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import com.theodo.springblueprint.features.users.domain.entities.User;
import java.time.Instant;

public interface UserTokensHelpers {
    TokenClaimsCodecPort tokenClaimsCodec();
    TimeProviderPort timeProvider();
    TokenProperties tokenProperties();

    default UserTokens newUserTokens(User user) {
        UserPrincipal userPrincipal = toUserPrincipal(user);
        AccessToken accessToken = newAccessToken(userPrincipal);
        return aUserTokens(accessToken);
    }

    default AccessToken newAccessToken(UserPrincipal userPrincipal) {
        Instant now = timeProvider().instant();
        TokenClaims tokenClaims = new TokenClaims(
            userPrincipal,
            now,
            now.plus(tokenProperties().accessTokenValidityDuration())
        );
        return tokenClaimsCodec().encode(tokenClaims);
    }
}
