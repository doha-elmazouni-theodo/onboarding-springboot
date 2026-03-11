package com.theodo.springblueprint.features.authentication.api.endpoints.login;

import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.features.authentication.domain.usecases.login.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginEndpointRequest(
    @NotBlank String username,

    @NotBlank String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(new Username(username()), password());
    }
}
