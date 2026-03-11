package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.common.domain.ports.PasswordEncoderPort;
import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;

public class FakePasswordEncoder implements PasswordEncoderPort {

    @Override
    public EncodedPassword encode(String password) {
        return new EncodedPassword("$encoded$" + password);
    }

    @Override
    public boolean matches(CharSequence plainPassword, EncodedPassword encodedPassword) {
        EncodedPassword encodedPlainPassword = encode(plainPassword.toString());
        return encodedPlainPassword.equals(encodedPassword);
    }
}
