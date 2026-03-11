package com.theodo.springblueprint.common.api;

import com.theodo.springblueprint.common.api.events.UnhandledExceptionEvent;
import com.theodo.springblueprint.common.domain.events.Event;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoggingEventListener {

    @EventListener
    public void logEvent(Event event) {
        Logger logger = LoggerFactory.getLogger(event.sourceType().getName());
        if (event instanceof UnhandledExceptionEvent unhandledExceptionEvent) {
            Exception exception = unhandledExceptionEvent.exception();
            logger.error(Objects.toString(exception), exception);
        } else {
            logger.info(event.toString());
        }
    }
}
