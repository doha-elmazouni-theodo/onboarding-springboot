package com.theodo.springblueprint.testhelpers.configurations;

import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class PropertiesTestConfiguration {

    @Bean
    public TokenProperties tokenProperties() {
        return new TokenProperties(true, Duration.ofSeconds(1), Duration.ofSeconds(10));
    }
}
