package com.theodo.springblueprint.features.users.domain.usecases.suts;

import com.theodo.springblueprint.common.domain.ports.PasswordEncoderPort;
import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakePasswordEncoder;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeRandomGenerator;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeUserRepository;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.usecases.signup.SignupCommand;
import com.theodo.springblueprint.features.users.domain.usecases.signup.SignupUseCase;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;

@Import({ SignupUseCase.class, SignupSut.Infra.class })
@RequiredArgsConstructor
public class SignupSut {

    private final SignupUseCase useCase;

    @Getter
    private final Infra infra;

    public User signup(final SignupCommand signupCommand) {
        return useCase.handle(signupCommand);
    }

    @Import(
        { FakeUserRepository.class, TimeTestConfiguration.class, FakePasswordEncoder.class, FakeRandomGenerator.class }
    )
    @RequiredArgsConstructor
    @Getter
    public static class Infra {

        private final FakeUserRepository userRepository;
        private final TimeProviderPort timeProvider;
        private final PasswordEncoderPort passwordEncoder;
    }
}
