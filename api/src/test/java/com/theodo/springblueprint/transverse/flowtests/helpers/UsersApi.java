package com.theodo.springblueprint.transverse.flowtests.helpers;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.function.Consumer;

public interface UsersApi {
    RestTestClient webTestClient();

    default FlowSession signupAsAdmin(String username, String password) {
        return signup(username, password, "ADMIN");
    }

    default FlowSession signupAsUser(String username, String password) {
        return signup(username, password, "USER");
    }

    default FlowSession getUsers(@Nullable Consumer<@Nullable Iterable<String>> assertions) {
        RestTestClient.ResponseSpec responseSpec = webTestClient()
            .get()
            .uri("/users")
            .exchange()
            .expectStatus()
            .isOk();
        if (assertions != null) {
            responseSpec.expectBody().jsonPath("$.users[*].username")
                .value(value -> {
                    @SuppressWarnings("unchecked")
                    List<String> usernames = (List<String>) value; // cast from Object
                    assertions.accept(usernames);
                });
        }
        return castThis();
    }

    default FlowSession getUsersShouldReturnStatus(HttpStatus status) {
        webTestClient().get().uri("/users").exchange().expectStatus().isEqualTo(status);
        return castThis();
    }

    private FlowSession castThis() {
        return (FlowSession) this;
    }

    private FlowSession signup(String username, String password, String role) {
        webTestClient()
            .post()
            .uri("/auth/public/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {"name":"Name", "username":"%s",
                "password":"%s", "roles":["%s"]}""".formatted(username, password, role)
            )
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .jsonPath("$.username")
            .isEqualTo(username);
        return castThis();
    }
}
