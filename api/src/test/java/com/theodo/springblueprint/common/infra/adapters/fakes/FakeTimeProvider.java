package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.common.infra.adapters.TimeProvider;
import java.time.Duration;

public class FakeTimeProvider extends TimeProvider {

    private final FakeClock clock;

    public FakeTimeProvider(FakeClock clock) {
        super(clock);
        this.clock = clock;
    }

    public void moveTime(Duration duration) {
        this.clock.moveTime(duration);
    }
}
