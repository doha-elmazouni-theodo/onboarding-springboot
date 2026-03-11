package com.theodo.springblueprint.transverse.flowtests.helpers;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

public interface AuthenticationApi {
    RestTestClient webTestClient();

    default FlowSession login(String username, String password) {
        webTestClient()
            .post()
            .uri("/auth/public/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {"username":"%s", "password":"%s"}""".formatted(username, password)
            )
            .exchange()
            .expectStatus()
            .isOk();
        return castThis();
    }

    default FlowSession refreshToken() {
        webTestClient()
            .post()
            .uri("/auth/public/refreshToken")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk();
        return castThis();
    }

    default FlowSession logout() {
        webTestClient()
            .post()
            .uri("/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk();
        return castThis();
    }

    private FlowSession castThis() {
        return (FlowSession) this;
    }
}
