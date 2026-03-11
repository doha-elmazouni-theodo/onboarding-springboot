package com.theodo.springblueprint.common.api.events;

import com.theodo.springblueprint.common.domain.events.Event;
import com.theodo.springblueprint.common.utils.StackTraceUtils;

public record UnhandledExceptionEvent(Exception exception) implements Event {
    @Override
    public Class<?> sourceType() {
        return StackTraceUtils.sourceTypeFromException(exception)
            .orElse(UnhandledExceptionEvent.class);
    }
}
