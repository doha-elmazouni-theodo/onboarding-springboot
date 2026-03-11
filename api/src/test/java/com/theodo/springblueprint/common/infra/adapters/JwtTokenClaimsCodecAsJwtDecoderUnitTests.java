package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.common.infra.adapters.fakes.FakeTimeProvider;
import com.theodo.springblueprint.features.authentication.domain.entities.TokenClaims;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import com.theodo.springblueprint.testhelpers.configurations.JwtTokenClaimsCodecTestConfiguration;
import java.time.Instant;
import java.time.temporal.ChronoField;

@UnitTest
public final class JwtTokenClaimsCodecAsJwtDecoderUnitTests extends SpringJwtDecoderContractTests {
    private final JwtTokenClaimsCodec jwtTokenClaimsCodec;

    public JwtTokenClaimsCodecAsJwtDecoderUnitTests(
        JwtTokenClaimsCodecTestConfiguration ignoredParamOnlyForContextLoading,
        JwtTokenClaimsCodec jwtTokenClaimsCodec,
        FakeTimeProvider timeProvider) {
        super(timeProvider, jwtTokenClaimsCodec);
        this.jwtTokenClaimsCodec = jwtTokenClaimsCodec;
    }

    @Override
    protected String encode(TokenClaims claims) {
        return jwtTokenClaimsCodec.encode(claims).value();
    }

    @Override
    protected String getMalformedToken() {
        return "invalidToken";
    }

    @Override
    // JWT only support time up to the seconds
    protected Instant adjustPrecision(Instant instant) {
        return instant.with(ChronoField.MILLI_OF_SECOND, 0);
    }

}
