package com.theodo.springblueprint.features.authentication.domain.usecases.suts;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeRandomGenerator;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeTokenClaimsCodec;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeUserRepository;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeUserSessionRepository;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.usecases.helpers.LoginHelpers;
import com.theodo.springblueprint.features.authentication.domain.usecases.recreateusertokens.RecreateUserTokensCommand;
import com.theodo.springblueprint.features.authentication.domain.usecases.recreateusertokens.RecreateUserTokensUseCase;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import com.theodo.springblueprint.testhelpers.configurations.PropertiesTestConfiguration;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;

// TODO: need to find a better class name
@Import({ RecreateUserTokensUseCase.class, RecreateUserTokensSut.Infra.class })
@RequiredArgsConstructor
public class RecreateUserTokensSut {

    private final RecreateUserTokensUseCase useCase;

    @Getter
    private final Infra infra;

    public UserTokens recreateUserTokens(RecreateUserTokensCommand recreateUserTokensCommand) {
        return useCase.handle(recreateUserTokensCommand);
    }

    @Import(
        {
            FakeUserSessionRepository.class,
            FakeUserRepository.class,
            FakeTokenClaimsCodec.class,
            PropertiesTestConfiguration.class,
            TimeTestConfiguration.class,
            FakeRandomGenerator.class,
        }
    )
    @RequiredArgsConstructor
    @Getter
    public static class Infra implements LoginHelpers {

        private final FakeUserRepository userRepository;
        private final FakeUserSessionRepository userSessionRepository;
        private final TokenProperties tokenProperties;
        private final FakeTokenClaimsCodec tokenClaimsCodec;
        private final TimeProviderPort timeProvider;
    }
}
