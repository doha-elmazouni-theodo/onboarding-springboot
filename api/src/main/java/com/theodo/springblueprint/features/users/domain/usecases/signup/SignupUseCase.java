package com.theodo.springblueprint.features.users.domain.usecases.signup;

import com.theodo.springblueprint.common.domain.ports.PasswordEncoderPort;
import com.theodo.springblueprint.common.domain.ports.RandomGeneratorPort;
import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.ports.UserRepositoryPort;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUser;
import java.time.Instant;

public class SignupUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TimeProviderPort timeProvider;
    private final RandomGeneratorPort randomGenerator;

    public SignupUseCase(
        final UserRepositoryPort userRepository,
        final PasswordEncoderPort passwordEncoder,
        final TimeProviderPort timeProvider, RandomGeneratorPort randomGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.timeProvider = timeProvider;
        this.randomGenerator = randomGenerator;
    }

    public User handle(final SignupCommand signupCommand) {
        EncodedPassword encodedPassword = passwordEncoder.encode(signupCommand.plainPassword());
        Instant creationDateTime = timeProvider.instant();
        final NewUser newUser = signupCommand.toNewUser(randomGenerator.uuid(), encodedPassword, creationDateTime);
        return userRepository.create(newUser);
    }
}
