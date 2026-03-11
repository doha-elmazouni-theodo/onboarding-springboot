package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.common.infra.adapters.SpringJwtDecoderContractTests;
import com.theodo.springblueprint.features.authentication.domain.entities.TokenClaims;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;

@UnitTest
public final class FakeTokenClaimsCodecAsJwtDecoderUnitTests extends SpringJwtDecoderContractTests {
    private final FakeTokenClaimsCodec fakeTokenClaimsCodec;

    public FakeTokenClaimsCodecAsJwtDecoderUnitTests(FakeTimeProvider timeProvider, FakeTokenClaimsCodec jwtDecoder) {
        super(timeProvider, jwtDecoder);
        fakeTokenClaimsCodec = jwtDecoder;
    }

    @Override
    protected String encode(TokenClaims claims) {
        return fakeTokenClaimsCodec.encode(claims).value();
    }

    @Override
    protected String getMalformedToken() {
        return "invalidToken";
    }
}
