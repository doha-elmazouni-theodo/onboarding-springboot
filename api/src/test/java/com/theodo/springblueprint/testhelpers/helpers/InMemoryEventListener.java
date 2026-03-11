package com.theodo.springblueprint.testhelpers.helpers;

import com.theodo.springblueprint.common.domain.events.Event;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.context.event.EventListener;

import java.util.concurrent.ConcurrentLinkedQueue;

public class InMemoryEventListener {

    private final ConcurrentLinkedQueue<Event> eventQueue = new ConcurrentLinkedQueue<>();

    @EventListener
    public void logEvent(Event event) {
        eventQueue.add(event);
    }

    public <T> ImmutableList<T> getEvents(Class<T> eventType) {
        return Immutable.list.fromStream(
            eventQueue.stream()
                .filter(eventType::isInstance)
                .map(e -> eventType.cast(e))
        );
    }
}
