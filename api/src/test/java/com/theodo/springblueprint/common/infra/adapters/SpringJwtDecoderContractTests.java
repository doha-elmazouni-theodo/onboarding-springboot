package com.theodo.springblueprint.common.infra.adapters;

import static com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipalBuilder.aUserPrincipal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.features.authentication.domain.entities.TokenClaims;
import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;
import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

@RequiredArgsConstructor
public abstract class SpringJwtDecoderContractTests {

    private final TimeProviderPort timeProvider;
    private final JwtDecoder jwtDecoder;

    @Test
    void returns_valid_Jwt_with_one_role() {
        TokenClaims claims = getTokenClaims(timeProvider.instant().plusSeconds(10), Role.USER);
        String token = encode(claims);

        // Act
        Jwt jwt = jwtDecoder.decode(token);

        assertThat(jwt)
            .returns(adjustPrecision(claims.expirationTime()), Jwt::getExpiresAt)
            .returns(adjustPrecision(claims.creationTime()), Jwt::getIssuedAt)
            .returns(List.of("USER"), decodedJwt -> decodedJwt.<List<String>>getClaim("scope"))
            .returns(getExpectedSubject(claims.userPrincipal()), Jwt::getSubject);
    }

    @Test
    void returns_valid_Jwt_without_role() {
        TokenClaims claims = getTokenClaims(timeProvider.instant().plusSeconds(10));
        String token = encode(claims);

        // Act
        Jwt jwt = jwtDecoder.decode(token);

        assertThat(jwt.<List<String>>getClaim("scope")).isEmpty();
    }

    @Test
    void returns_valid_Jwt_with_two_roles() {
        TokenClaims claims = getTokenClaims(timeProvider.instant().plusSeconds(1), Role.USER, Role.ADMIN);
        String token = encode(claims);

        // Act
        Jwt jwt = jwtDecoder.decode(token);

        assertThatObject(jwt.getClaim("scope"))
            .asInstanceOf(InstanceOfAssertFactories.iterable(String.class))
            .containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    void throws_JwtValidationException_if_expired() {
        TokenClaims claims = getTokenClaims(timeProvider.instant().minusSeconds(1), Role.USER);
        String token = encode(claims);

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> jwtDecoder.decode(token));

        exceptionAssertion
            .isExactlyInstanceOf(JwtValidationException.class)
            .extracting(
                SpringJwtDecoderContractTests::getJwtValidationErrorTypes,
                InstanceOfAssertFactories.list(Class.class)
            )
            .containsExactly(OAuth2Error.class);
    }

    @Test
    void throws_BadJwtException_if_malformed() {
        String token = getMalformedToken();

        // Act
        var exceptionAssertion = assertThatThrownBy(() -> jwtDecoder.decode(token));

        exceptionAssertion
            .isInstanceOf(BadJwtException.class);
    }

    private String getExpectedSubject(UserPrincipal userPrincipal) {
        return "%s,%s".formatted(userPrincipal.id(), userPrincipal.username().value());
    }

    private static List<Class<?>> getJwtValidationErrorTypes(Throwable exception) {
        JwtValidationException validationException = (JwtValidationException) exception;
        return validationException.getErrors()
            .stream()
            .<Class<?>>map(error -> error.getClass())
            .toList();
    }

    private TokenClaims getTokenClaims(Instant expirationTime, Role... roles) {
        return new TokenClaims(
            aUserPrincipal().roles(Immutable.set.of(roles)).build(),
            expirationTime.minusSeconds(1),
            expirationTime
        );
    }

    // --------------------------------- Protected Methods ------------------------------- //
    protected abstract String encode(TokenClaims claims);

    protected abstract String getMalformedToken();

    protected Instant adjustPrecision(Instant instant) {
        return instant;
    }
}
