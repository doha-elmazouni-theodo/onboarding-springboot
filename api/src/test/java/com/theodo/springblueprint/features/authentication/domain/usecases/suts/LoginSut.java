package com.theodo.springblueprint.features.authentication.domain.usecases.suts;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.infra.adapters.fakes.*;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.usecases.helpers.UserCreationHelpers;
import com.theodo.springblueprint.features.authentication.domain.usecases.login.LoginCommand;
import com.theodo.springblueprint.features.authentication.domain.usecases.login.LoginUseCase;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import com.theodo.springblueprint.testhelpers.configurations.PropertiesTestConfiguration;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;

@Import({ LoginUseCase.class, LoginSut.Infra.class })
@RequiredArgsConstructor
public class LoginSut {

    private final LoginUseCase useCase;

    @Getter
    private final Infra infra;

    public UserTokens login(final LoginCommand loginCommand) {
        return useCase.handle(loginCommand);
    }

    @Import(
        {
            FakeUserSessionRepository.class,
            FakeUserRepository.class,
            FakeTokenClaimsCodec.class,
            PropertiesTestConfiguration.class,
            TimeTestConfiguration.class,
            FakeEventPublisher.class,
            FakePasswordEncoder.class,
            FakeRandomGenerator.class,
        }
    )
    @RequiredArgsConstructor
    @Getter
    public static class Infra implements UserCreationHelpers {

        public final FakeUserRepository userRepository;
        public final FakeTokenClaimsCodec tokenClaimsCodec;
        public final TokenProperties tokenProperties;
        public final TimeProviderPort timeProvider;
        public final FakeEventPublisher eventPublisher;
        public final FakeUserSessionRepository userSessionRepository;
    }
}
