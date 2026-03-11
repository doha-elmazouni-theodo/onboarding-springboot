package com.theodo.springblueprint.common.domain.ports;

import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;

public interface PasswordEncoderPort {
    EncodedPassword encode(String password);
    boolean matches(CharSequence plainPassword, EncodedPassword encodedPassword);
}
