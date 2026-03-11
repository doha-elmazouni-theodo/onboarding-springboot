package com.theodo.springblueprint.features.authentication.domain.usecases.login;

import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;
import com.theodo.springblueprint.common.domain.ports.PasswordEncoderPort;
import com.theodo.springblueprint.common.domain.ports.RandomGeneratorPort;
import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.features.authentication.domain.entities.UserCredential;
import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;
import com.theodo.springblueprint.features.authentication.domain.events.UserLoggedInEvent;
import com.theodo.springblueprint.features.authentication.domain.exceptions.BadUserCredentialException;
import com.theodo.springblueprint.features.authentication.domain.exceptions.UnknownUsernameException;
import com.theodo.springblueprint.features.authentication.domain.ports.TokenClaimsCodecPort;
import com.theodo.springblueprint.features.authentication.domain.ports.UserCredentialsRepositoryPort;
import com.theodo.springblueprint.features.authentication.domain.ports.UserSessionRepositoryPort;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.services.RefreshTokenService;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;

public class LoginUseCase {

    private final UserCredentialsRepositoryPort userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoderPort passwordEncoder;
    private final EventPublisherPort eventPublisher;

    public LoginUseCase(
        final UserCredentialsRepositoryPort userRepository,
        final PasswordEncoderPort passwordEncoder,
        final EventPublisherPort eventPublisher,
        /* RefreshTokenService dependencies */
        final UserSessionRepositoryPort userSessionRepository,
        final TokenClaimsCodecPort tokenClaimsCodec,
        final TokenProperties tokenProperties,
        final TimeProviderPort timeProvider,
        final RandomGeneratorPort randomGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.refreshTokenService = new RefreshTokenService(
            userSessionRepository,
            tokenClaimsCodec,
            tokenProperties,
            timeProvider,
            randomGenerator
        );
    }

    public UserTokens handle(final LoginCommand loginCommand) {
        final UserCredential userCredential = userRepository
            .findUserCredentialByUsername(loginCommand.username())
            .orElseThrow(() -> new UnknownUsernameException(loginCommand.username()));
        if (!passwordEncoder.matches(loginCommand.password(), userCredential.encodedPassword())) {
            throw new BadUserCredentialException(loginCommand.username());
        }
        UserPrincipal userPrincipal = userCredential.userPrincipal();
        final UserTokens userTokens = refreshTokenService.createUserTokens(userPrincipal);
        eventPublisher.publishEvent(new UserLoggedInEvent(userPrincipal, this.getClass()));
        return userTokens;
    }
}
