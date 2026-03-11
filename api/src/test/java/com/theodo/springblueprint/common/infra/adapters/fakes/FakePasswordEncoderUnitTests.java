package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.common.domain.ports.PasswordEncoderPortContractTests;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;

@UnitTest
public class FakePasswordEncoderUnitTests extends PasswordEncoderPortContractTests {
    public FakePasswordEncoderUnitTests(FakePasswordEncoder passwordEncoder) {
        super(passwordEncoder);
    }
}
