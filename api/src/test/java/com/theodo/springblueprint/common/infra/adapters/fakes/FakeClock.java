package com.theodo.springblueprint.common.infra.adapters.fakes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class FakeClock extends Clock {

    private Instant instant;
    private final ZoneId zone;

    public FakeClock(Instant instant, ZoneId zone) {
        super();
        this.instant = instant;
        this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
        return this.zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new FakeClock(this.instant, zone);
    }

    @Override
    public Instant instant() {
        return this.instant;
    }

    public void moveTime(Duration duration) {
        this.instant = this.instant.plus(duration);
    }
}
