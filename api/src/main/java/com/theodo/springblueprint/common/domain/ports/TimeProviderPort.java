package com.theodo.springblueprint.common.domain.ports;

import java.time.Instant;

public interface TimeProviderPort {
    Instant instant();
}
