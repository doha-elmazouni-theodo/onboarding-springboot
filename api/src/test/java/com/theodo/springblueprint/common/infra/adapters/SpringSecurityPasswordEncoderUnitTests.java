package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.common.api.beans.InfraBeanConfiguration;
import com.theodo.springblueprint.common.domain.ports.PasswordEncoderPort;
import com.theodo.springblueprint.common.domain.ports.PasswordEncoderPortContractTests;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;

@UnitTest
public class SpringSecurityPasswordEncoderUnitTests extends PasswordEncoderPortContractTests {

    private static final PasswordEncoderPort INSTANCE = new SpringSecurityPasswordEncoder(
        new InfraBeanConfiguration().passwordEncoder()
    );

    public SpringSecurityPasswordEncoderUnitTests() {
        super(INSTANCE);
    }
}
