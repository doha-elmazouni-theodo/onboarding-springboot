package com.theodo.springblueprint.common.domain.ports;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
public abstract class RandomGeneratorPortContractTests {
    private final RandomGeneratorPort randomGenerator;

    @Test
    void generating_a_huge_number_of_random_uuid_returns_0_duplicates() {
        int count = 10_000;

        // Act
        Set<UUID> uuids = IntStream.range(0, count)
            .mapToObj(index -> randomGenerator.uuid())
            .collect(Collectors.toSet());

        assertThat(uuids).hasSize(count);
    }
}
