package com.theodo.springblueprint.features.authentication.domain.usecases.suts;

import com.theodo.springblueprint.common.infra.adapters.fakes.FakeTimeProvider;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeUserRepository;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeUserSessionRepository;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.usecases.helpers.UserCreationHelpers;
import com.theodo.springblueprint.features.authentication.domain.usecases.helpers.UserSessionHelpers;
import com.theodo.springblueprint.features.authentication.domain.usecases.purgerefreshtokens.PurgeRefreshTokensUseCase;
import com.theodo.springblueprint.testhelpers.configurations.PropertiesTestConfiguration;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;

@Import({ PurgeRefreshTokensUseCase.class, PurgeRefreshTokensSut.Infra.class })
@RequiredArgsConstructor
public class PurgeRefreshTokensSut {

    private final PurgeRefreshTokensUseCase useCase;

    @Getter
    private final Infra infra;

    public void purgeRefreshToken() {
        useCase.handle();
    }

    @Import(
        {
            FakeUserRepository.class,
            FakeUserSessionRepository.class,
            TimeTestConfiguration.class,
            PropertiesTestConfiguration.class,
        }
    )
    @RequiredArgsConstructor
    @Getter
    public static class Infra implements UserCreationHelpers, UserSessionHelpers {

        private final FakeUserRepository userRepository;
        private final FakeUserSessionRepository userSessionRepository;
        private final FakeTimeProvider timeProvider;
        private final TokenProperties tokenProperties;
    }
}
