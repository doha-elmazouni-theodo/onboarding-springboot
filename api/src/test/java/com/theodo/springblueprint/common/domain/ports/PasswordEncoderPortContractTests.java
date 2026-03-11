package com.theodo.springblueprint.common.domain.ports;

import static org.assertj.core.api.Assertions.assertThat;

import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
public abstract class PasswordEncoderPortContractTests {

    private final PasswordEncoderPort passwordEncoder;

    @Nested
    class Encode {

        @Test
        void encoding_a_string_returns_an_EncodedPassword_whose_value_differ_from_the_original_string() {
            String password = "password";

            // Act
            EncodedPassword encodedPassword = passwordEncoder.encode(password);

            assertThat(encodedPassword.value()).isNotEqualTo(password);
        }
    }

    @Nested
    class Matches {

        @Test
        void matching_a_string_with_its_encoded_version_returns_true() {
            String password = "password";
            EncodedPassword encodedPassword = passwordEncoder.encode(password);

            // Act
            boolean isMatch = passwordEncoder.matches(password, encodedPassword);

            assertThat(isMatch).isTrue();
        }

        @Test
        void matching_a_string_with_the_encoded_version_of_another_string_returns_false() {
            String password = "password";
            EncodedPassword anotherEncodedPassword = passwordEncoder.encode("another_password");

            // Act
            boolean isMatch = passwordEncoder.matches(password, anotherEncodedPassword);

            assertThat(isMatch).isFalse();
        }

        @Test
        void matching_password_with_malformed_encoded_password_returns_false() {
            String password = "password";
            EncodedPassword invalidEncodedPassword = new EncodedPassword(UUID.randomUUID().toString());

            // Act
            boolean isMatch = passwordEncoder.matches(password, invalidEncodedPassword);

            assertThat(isMatch).isFalse();
        }
    }
}
