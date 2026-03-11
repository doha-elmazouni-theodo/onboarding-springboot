package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.common.domain.ports.TimeProviderPort;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoField;
import org.springframework.stereotype.Service;

@Service
public class TimeProvider implements TimeProviderPort {

    public static final TimeProviderPort UTC = new TimeProvider(Clock.systemUTC());

    private final Clock clock;

    public TimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant instant() {
        return adjustPrecision(clock.instant());
    }

    // Java's Instant time precision is higher than Postgres one
    // This method aims to reduce the precision to align with the DB
    // Ex: executing "SELECT CAST('2024-06-18T09:54:18.123456789Z' AS TIMESTAMP)"
    // in Postgres will return "2024-06-18 09:54:18.123457"
    // TODO: Add DB integration test to make sure we use the right precision
    private Instant adjustPrecision(Instant instant) {
        return instant.with(ChronoField.MICRO_OF_SECOND, instant.get(ChronoField.MICRO_OF_SECOND));
    }
}
