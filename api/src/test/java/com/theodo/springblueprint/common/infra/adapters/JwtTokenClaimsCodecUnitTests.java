package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.common.infra.adapters.fakes.FakeTimeProvider;
import com.theodo.springblueprint.features.authentication.domain.ports.TokenClaimsCodecPortContractTests;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.AccessToken;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.theodo.springblueprint.testhelpers.configurations.JwtTokenClaimsCodecTestConfiguration;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

@UnitTest
public class JwtTokenClaimsCodecUnitTests extends TokenClaimsCodecPortContractTests {
    private static final String ISSUER = "myapp";
    private static final String SIGNING_KEY_STRING = "zdtlD3JK56m6wTTgsNFhqzjqPaaaddingFor256bits=";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SIGNING_KEY_STRING));

    private final FakeTimeProvider timeProvider;

    public JwtTokenClaimsCodecUnitTests(
        JwtTokenClaimsCodecTestConfiguration ignoredParamOnlyForContextLoading,
        JwtTokenClaimsCodec tokenCodec,
        FakeTimeProvider timeProvider) {
        super(tokenCodec, timeProvider);
        this.timeProvider = timeProvider;
    }

    @Override
    protected AccessToken getTokenWithoutRoleAttribute() {
        String jwt = getJwtBuilder().compact();
        return new AccessToken(jwt);
    }

    @Override
    protected AccessToken getTokenWithInvalidRolesArrayClaim() {
        String jwt = getJwtBuilder().claim("scope", new int[] { 1, 2, 3 }).compact();
        return new AccessToken(jwt);
    }

    @Override
    protected AccessToken getTokenWithInvalidRolesScalarClaim() {
        String jwt = getJwtBuilder().claim("scope", 10).compact();
        return new AccessToken(jwt);
    }

    @Override
    protected Instant adjustPrecision(Instant instant) {
        return instant.truncatedTo(ChronoUnit.SECONDS);
    }

    private JwtBuilder getJwtBuilder() {
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(UUID.randomUUID().toString())
            .issuer(ISSUER)
            .signWith(SIGNING_KEY)
            .issuedAt(Date.from(timeProvider.instant()))
            .expiration(Date.from(timeProvider.instant().plusSeconds(1)));
    }
}
