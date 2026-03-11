package com.theodo.springblueprint.common.domain.ports;

import com.theodo.springblueprint.common.domain.events.Event;

public interface EventPublisherPort {
    void publishEvent(Event event);
}
