package com.theodo.springblueprint.testhelpers.helpers;

import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import com.theodo.springblueprint.testhelpers.utils.StringUtils;

public interface TokenHelpers {
    static String urlEncodeAccessToken(UserTokens userTokens) {
        return StringUtils.urlEncode(userTokens.accessToken().value());
    }

    static String urlEncodeRefreshToken(UserTokens userTokens) {
        return StringUtils.urlEncode(userTokens.refreshToken().value());
    }
}
