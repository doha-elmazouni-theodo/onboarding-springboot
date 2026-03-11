package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.common.domain.ports.PasswordEncoderPort;
import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

@Component
public class SpringSecurityPasswordEncoder implements PasswordEncoderPort {

    private final PasswordEncoder passwordEncoder;

    public SpringSecurityPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EncodedPassword encode(String password) {
        // castNonNull: PasswordEncoder.encore(password) returns null only if password is null
        String encodedPassword = castNonNull(passwordEncoder.encode(password));
        return new EncodedPassword(encodedPassword);
    }

    @Override
    public boolean matches(CharSequence plainPassword, EncodedPassword encodedPassword) {
        return passwordEncoder.matches(plainPassword, encodedPassword.value());
    }
}
