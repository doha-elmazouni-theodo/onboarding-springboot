package com.theodo.springblueprint.features.authentication.domain.ports;

import static com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipalBuilder.aUserPrincipal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.features.authentication.domain.entities.TokenClaims;
import com.theodo.springblueprint.features.authentication.domain.exceptions.AccessTokenDecodingException;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
public abstract class TokenClaimsCodecPortContractTests {

    private final TokenClaimsCodecPort tokenCodec;
    private final TimeProviderPort timeProvider;

    @Nested
    class Encode {

        @Test
        void encoding_a_valid_claim_returns_a_non_null_accesstoken() {
            TokenClaims claims = getTokenClaims(Role.USER);

            // Act
            AccessToken token = tokenCodec.encode(claims);

            assertThat(token).isNotNull();
        }

        @Test
        void encoding_the_same_claim_twice_returns_different_accesstokens() {
            TokenClaims claims = getTokenClaims(Role.USER);
            AccessToken firstToken = tokenCodec.encode(claims);

            // Act
            AccessToken secondToken = tokenCodec.encode(claims);

            assertThat(secondToken).isNotEqualTo(firstToken);
        }
    }

    @Nested
    class DecodeWithoutExpirationValidation {

        @Test
        void decoding_a_valid_accesstoken_with_one_role_returns_the_initial_claim() {
            TokenClaims claims = getTokenClaims(Role.USER);
            AccessToken token = tokenCodec.encode(claims);

            // Act
            TokenClaims decodedClaims = tokenCodec.decodeWithoutExpirationValidation(token);

            assertThat(decodedClaims)
                .returns(claims.userPrincipal(), TokenClaims::userPrincipal)
                .returns(adjustPrecision(claims.creationTime()), TokenClaims::creationTime)
                .returns(adjustPrecision(claims.expirationTime()), TokenClaims::expirationTime);
        }

        @Test
        void decoding_a_valid_accesstoken_with_two_roles_returns_the_initial_claim() {
            TokenClaims claims = getTokenClaims(Role.USER, Role.ADMIN);
            AccessToken token = tokenCodec.encode(claims);

            // Act
            TokenClaims decodedClaims = tokenCodec.decodeWithoutExpirationValidation(token);

            assertThat(decodedClaims.userPrincipal().roles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        }

        @Test
        void decoding_a_valid_accesstoken_with_empty_roles_returns_the_initial_claim() {
            TokenClaims claims = getTokenClaims();
            AccessToken token = tokenCodec.encode(claims);

            // Act
            TokenClaims decodedClaims = tokenCodec.decodeWithoutExpirationValidation(token);

            assertThat(decodedClaims.userPrincipal().roles()).isEmpty();
        }

        @Test
        void decoding_an_expired_accesstoken_returns_the_initial_claim() {
            Instant epochZero = Instant.ofEpochMilli(0);
            TokenClaims claims = new TokenClaims(aUserPrincipal().build(), epochZero, epochZero.plusSeconds(1));
            AccessToken token = tokenCodec.encode(claims);

            // Act
            TokenClaims decodedClaims = tokenCodec.decodeWithoutExpirationValidation(token);

            assertThat(decodedClaims).isEqualTo(claims);
        }

        @Test
        void decoding_an_malformed_accesstoken_throws_AccessTokenDecodingException() {
            AccessToken token = new AccessToken("abc");

            // Act
            var exceptionAssertion = assertThatThrownBy(() -> tokenCodec.decodeWithoutExpirationValidation(token));

            exceptionAssertion
                .isInstanceOf(AccessTokenDecodingException.class)
                .hasMessage("Cannot decode token 'abc'");
        }

        @Test
        void decoding_an_accesstoken_without_roles_throws_AccessTokenDecodingException() {
            AccessToken token = getTokenWithoutRoleAttribute();

            // Act
            var exceptionAssertion = assertThatThrownBy(() -> tokenCodec.decodeWithoutExpirationValidation(token));

            exceptionAssertion
                .isInstanceOf(AccessTokenDecodingException.class)
                .hasMessageStartingWith("Cannot decode token '" + token.value() + "'");
        }

        @Test
        void decoding_an_accesstoken_with_invalid_roles_array_claim_throws_AccessTokenDecodingException() {
            AccessToken token = getTokenWithInvalidRolesArrayClaim();

            // Act
            var exceptionAssertion = assertThatThrownBy(() -> tokenCodec.decodeWithoutExpirationValidation(token));

            exceptionAssertion
                .isInstanceOf(AccessTokenDecodingException.class)
                .hasMessageStartingWith("Cannot decode token '" + token.value() + "'");
        }

        @Test
        void decoding_an_accesstoken_with_invalid_roles_scalar_claim_throws_AccessTokenDecodingException() {
            AccessToken token = getTokenWithInvalidRolesScalarClaim();

            // Act
            var exceptionAssertion = assertThatThrownBy(() -> tokenCodec.decodeWithoutExpirationValidation(token));

            exceptionAssertion
                .isInstanceOf(AccessTokenDecodingException.class)
                .hasMessageStartingWith("Cannot decode token '" + token.value() + "'");
        }
    }

    private TokenClaims getTokenClaims(Role... roles) {
        Instant now = timeProvider.instant();
        return new TokenClaims(aUserPrincipal().roles(Immutable.set.of(roles)).build(), now, now.plusSeconds(1));
    }

    // --------------------------------- Protected Methods ------------------------------- //

    protected abstract AccessToken getTokenWithoutRoleAttribute();

    protected abstract AccessToken getTokenWithInvalidRolesArrayClaim();

    protected abstract AccessToken getTokenWithInvalidRolesScalarClaim();

    protected Instant adjustPrecision(Instant instant) {
        return instant;
    }
}
