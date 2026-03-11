package com.theodo.springblueprint.testhelpers.configurations;

import com.theodo.springblueprint.common.infra.adapters.JwtTokenClaimsCodec;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeRandomGenerator;
import com.theodo.springblueprint.common.infra.properties.JwtProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({ JwtTokenClaimsCodec.class, TimeTestConfiguration.class, FakeRandomGenerator.class })
@TestConfiguration
public class JwtTokenClaimsCodecTestConfiguration {

    @Bean
    public JwtProperties getJwtProperties() {
        return new JwtProperties("zdtlD3JK56m6wTTgsNFhqzjqPaaaddingFor256bits=", "myapp");
    }
}
