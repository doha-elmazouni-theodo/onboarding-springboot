package com.theodo.springblueprint.testhelpers.configurations;

import com.theodo.springblueprint.common.infra.adapters.fakes.FakeClock;
import com.theodo.springblueprint.common.infra.adapters.fakes.FakeTimeProvider;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TimeTestConfiguration {

    @Bean
    public static FakeClock fakeClock() {
        Instant instant = Instant.parse("2022-01-01T00:00:00.123Z");
        return new FakeClock(instant, ZoneId.of("UTC"));
    }

    @Bean
    public static FakeTimeProvider fakeTimeProvider(FakeClock fakeClock) {
        return new FakeTimeProvider(fakeClock);
    }

    // ⚠️ Call this method directly only in classes without bean injection, like Fixture builders.
    // Otherwise, inject and use FakeTimeProvider bean.
    public static FakeTimeProvider fakeTimeProvider() {
        return fakeTimeProvider(fakeClock());
    }
}
