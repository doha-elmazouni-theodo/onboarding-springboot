package com.theodo.springblueprint.features.authentication.api.endpoints.login;

import com.theodo.springblueprint.features.authentication.api.services.AuthenticationResponseEntity;
import com.theodo.springblueprint.features.authentication.domain.properties.TokenProperties;
import com.theodo.springblueprint.features.authentication.domain.usecases.login.LoginUseCase;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.UserTokens;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginEndpoint {

    private static final String URL = "/auth/public/login";

    private final LoginUseCase loginUseCase;
    private final AuthenticationResponseEntity authenticationResponseEntity;

    public LoginEndpoint(
        final LoginUseCase loginUseCase,
        @Value("${server.servlet.context-path}") final String apiContextPath,
        final TokenProperties tokenProperties) {
        this.loginUseCase = loginUseCase;
        this.authenticationResponseEntity = new AuthenticationResponseEntity(apiContextPath, tokenProperties);
    }

    @PostMapping(URL)
    public ResponseEntity<Void> login(@RequestBody @Valid final LoginEndpointRequest loginEndpointRequest) {
        final UserTokens userTokens = loginUseCase.handle(loginEndpointRequest.toCommand());
        return authenticationResponseEntity.from(userTokens);
    }
}
