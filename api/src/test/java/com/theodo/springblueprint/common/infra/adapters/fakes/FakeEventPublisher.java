package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.common.domain.events.Event;
import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;
import com.theodo.springblueprint.common.utils.collections.Mutable;
import java.util.Optional;
import org.eclipse.collections.api.list.MutableList;

public class FakeEventPublisher implements EventPublisherPort {

    private final MutableList<Event> events = Mutable.list.empty();

    @Override
    public void publishEvent(Event event) {
        events.add(event);
    }

    public <T extends Event> Optional<T> lastEvent(Class<T> eventType) {
        for (int i = events.size() - 1; i >= 0; i--) {
            Event event = events.get(i);
            if (eventType.isInstance(event)) {
                return Optional.of(eventType.cast(event));
            }
        }
        return Optional.empty();
    }
}
