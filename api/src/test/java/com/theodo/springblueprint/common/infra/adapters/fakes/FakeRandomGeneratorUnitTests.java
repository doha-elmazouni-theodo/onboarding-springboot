package com.theodo.springblueprint.common.infra.adapters.fakes;

import static org.assertj.core.api.Assertions.assertThat;

import com.theodo.springblueprint.common.domain.ports.RandomGeneratorPortContractTests;
import com.theodo.springblueprint.testhelpers.annotations.UnitTest;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

@UnitTest
class FakeRandomGeneratorUnitTests extends RandomGeneratorPortContractTests {

    public FakeRandomGeneratorUnitTests(FakeRandomGenerator randomGenerator) {
        super(randomGenerator);
    }

    @Test
    void FakeRandomGenerator_is_deterministic() {
        FakeRandomGenerator fakeRandomGenerator1 = new FakeRandomGenerator();
        FakeRandomGenerator fakeRandomGenerator2 = new FakeRandomGenerator();
        int count = 10_000;

        List<UUID> firstUuids = IntStream.range(0, count)
            .mapToObj(index -> fakeRandomGenerator1.uuid())
            .toList();

        // Act
        List<UUID> secondUuids = IntStream.range(0, count)
            .mapToObj(index -> fakeRandomGenerator2.uuid())
            .toList();

        assertThat(secondUuids).containsExactlyElementsOf(firstUuids);
    }
}
