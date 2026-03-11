package com.theodo.springblueprint.common.api.security;

import com.theodo.springblueprint.features.authentication.api.services.AuthenticationResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class StandaloneJwtAuthentication {

    private final JwtDecoder jwtDecoder;
    private final AuthenticationProblemEntryPoint authEntryPoint;

    public StandaloneJwtAuthentication(JwtDecoder jwtDecoder, AuthenticationProblemEntryPoint authEntryPoint) {
        this.jwtDecoder = jwtDecoder;
        this.authEntryPoint = authEntryPoint;
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void configure(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(this::localMode);
    }

    private void localMode(OAuth2ResourceServerConfigurer<HttpSecurity> oauth2) {
        oauth2
            .authenticationEntryPoint(authEntryPoint)
            .bearerTokenResolver(AuthenticationResponseEntity::retrieveAccessToken)
            .jwt(jwtConfigurer -> {
                jwtConfigurer.jwtAuthenticationConverter(getConverterWithRoleAuthorityPrefix());
                jwtConfigurer.decoder(jwtDecoder);
            });
    }

    private static JwtAuthenticationConverter getConverterWithRoleAuthorityPrefix() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
