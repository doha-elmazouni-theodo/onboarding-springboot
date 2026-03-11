package com.theodo.springblueprint.common.infra.adapters.fakes;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.LogicalType;
import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.infra.mappers.RoleMapper;
import com.theodo.springblueprint.common.utils.collections.Mutable;
import com.theodo.springblueprint.features.authentication.domain.entities.TokenClaims;
import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;
import com.theodo.springblueprint.features.authentication.domain.exceptions.AccessTokenDecodingException;
import com.theodo.springblueprint.features.authentication.domain.ports.TokenClaimsCodecPort;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.testhelpers.configurations.TimeTestConfiguration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import tools.jackson.datatype.eclipsecollections.EclipseCollectionsModule;

@Import(TimeTestConfiguration.class)
public class FakeTokenClaimsCodec implements TokenClaimsCodecPort, JwtDecoder {

    private static final ObjectMapper objectMapper = createObjectMapper();
    private final TimeProviderPort timeProvider;
    @Nullable private AccessToken lastCreatedToken;

    public FakeTokenClaimsCodec(TimeProviderPort timeProvider) {
        this.timeProvider = timeProvider;
    }

    @SneakyThrows
    @Override
    public AccessToken encode(TokenClaims tokenClaims) {
        ClaimWrapper claimWrapper = new ClaimWrapper(UUID.randomUUID(), tokenClaims);
        String tokenJson = objectMapper.writeValueAsString(claimWrapper);
        lastCreatedToken = new AccessToken(tokenJson);
        return lastCreatedToken;
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("nullness:nulltest.redundant")
    public TokenClaims decodeWithoutExpirationValidation(AccessToken token) {
        ClaimWrapper claimWrapper;
        try {
            claimWrapper = objectMapper.readValue(token.value(), ClaimWrapper.class);
        } catch (MismatchedInputException | StreamReadException exception) {
            throw new AccessTokenDecodingException(exception, token);
        }
        if (claimWrapper.tokenClaims.userPrincipal().roles() == null) {
            throw new AccessTokenDecodingException("missing claim 'scope'", token);
        }
        return claimWrapper.tokenClaims();
    }

    public Optional<AccessToken> lastCreatedToken() {
        return Optional.ofNullable(lastCreatedToken);
    }

    @Override
    public Jwt decode(String token) {
        TokenClaims tokenClaims;
        try {
            tokenClaims = decodeWithoutExpirationValidation(new AccessToken(token));
        } catch (AccessTokenDecodingException e) {
            throw new BadJwtException("Cannot parse JWT", e);
        }
        if (tokenClaims.expirationTime().isBefore(timeProvider.instant())) {
            throw new JwtValidationException("", List.of(new OAuth2Error("invalid_token")));
        }

        UserPrincipal user = tokenClaims.userPrincipal();
        List<String> roles = Mutable.collectList(user.roles(), RoleMapper.INSTANCE::fromValueObject);
        String subject = "%s,%s".formatted(user.id(), user.username().value());
        return Jwt.withTokenValue(token)
            .header("alg", "none")
            .claim("scope", roles)
            .expiresAt(tokenClaims.expirationTime())
            .issuedAt(tokenClaims.creationTime())
            .subject(subject)
            .build();
    }

    private static ObjectMapper createObjectMapper() {
        JsonMapper.Builder builder = JsonMapper.builder().addModule(new EclipseCollectionsModule())
            .withCoercionConfig(
                LogicalType.Textual,
                c -> c.setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
            );

        return builder.build();
    }

    private record ClaimWrapper(UUID rand, TokenClaims tokenClaims) {
    }
}
