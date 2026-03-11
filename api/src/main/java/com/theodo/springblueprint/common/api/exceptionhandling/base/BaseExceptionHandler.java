package com.theodo.springblueprint.common.api.exceptionhandling.base;

import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;
import jakarta.annotation.Nullable;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

@Order(0)
public abstract class BaseExceptionHandler {

    private final InternalResponseEntityExceptionHandler baseExceptionHandler;

    protected BaseExceptionHandler(EventPublisherPort eventPublisher) {
        this.baseExceptionHandler = new InternalResponseEntityExceptionHandler(eventPublisher);
    }

    @Nullable protected ResponseEntity<Object> getDefaultResponseEntity(
        Exception ex,
        WebRequest request,
        HttpStatusCode status,
        ErrorCode errorCode) {
        return baseExceptionHandler.getDefaultResponseEntity(ex, request, status, errorCode);
    }

    // This is an inner class to avoid polluting the BaseExceptionHandler subclasses with handlers from Spring
    private static class InternalResponseEntityExceptionHandler extends BaseResponseEntityExceptionHandler {

        protected InternalResponseEntityExceptionHandler(EventPublisherPort eventPublisher) {
            super(eventPublisher);
        }
    }
}
