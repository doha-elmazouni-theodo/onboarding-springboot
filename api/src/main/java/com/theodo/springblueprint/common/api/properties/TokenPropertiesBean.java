package com.theodo.springblueprint.common.api.properties;

import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Configuration
public class TokenPropertiesBean {

    @Bean
    TokenProperties tokenProperties(InternalTokenProperties internalTokenProperties) {
        return new TokenProperties(
            internalTokenProperties.getHttps(),
            Duration.ofSeconds(internalTokenProperties.getAccessTokenDurationInSeconds()),
            Duration.ofSeconds(internalTokenProperties.getRefreshTokenDurationInSeconds())
        );
    }

    @Data
    @Validated
    @ConfigurationProperties(prefix = "security")
    @Component
    @Accessors(fluent = false) // required for @ConfigurationProperties
    private static final class InternalTokenProperties {

        @NotNull private Boolean https;

        @NotNull private Integer accessTokenDurationInSeconds;

        @NotNull private Integer refreshTokenDurationInSeconds;
    }
}
