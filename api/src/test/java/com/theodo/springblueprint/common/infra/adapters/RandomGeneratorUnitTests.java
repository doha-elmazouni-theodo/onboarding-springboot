package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.common.domain.ports.RandomGeneratorPortContractTests;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;

@UnitTest
public class RandomGeneratorUnitTests extends RandomGeneratorPortContractTests {
    public RandomGeneratorUnitTests(RandomGenerator randomGenerator) {
        super(randomGenerator);
    }
}
