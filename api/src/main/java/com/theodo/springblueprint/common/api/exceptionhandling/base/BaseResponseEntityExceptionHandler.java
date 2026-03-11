package com.theodo.springblueprint.common.api.exceptionhandling.base;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

import com.theodo.springblueprint.common.api.events.UnhandledExceptionEvent;
import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;
import java.net.URI;
import java.util.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(0)
public abstract class BaseResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final ErrorAttributes ERROR_ATTRIBUTES_PROVIDER = new DefaultErrorAttributes();
    private final EventPublisherPort eventPublisher;

    protected BaseResponseEntityExceptionHandler(EventPublisherPort eventPublisher) {
        super();
        this.eventPublisher = eventPublisher;
    }

    @Nullable protected ResponseEntity<Object> getDefaultResponseEntity(
        Exception ex,
        WebRequest request,
        HttpStatusCode status,
        ErrorCode errorCode) {
        ProblemDetail body = this.createProblemDetail(ex, status, errorCode.code(), null, null, request);
        body.setTitle(errorCode.code());
        return this.handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    @Override
    @Nullable protected ResponseEntity<Object> handleExceptionInternal(
        @NotNull Exception ex,
        @Nullable Object body,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode statusCode,
        @NotNull WebRequest request) {
        eventPublisher.publishEvent(new UnhandledExceptionEvent(ex));
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    @Override
    protected @NonNull ResponseEntity<Object> createResponseEntity(
        @Nullable Object body,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode statusCode,
        @NotNull WebRequest request) {
        ProblemDetail problemDetail = (ProblemDetail) castNonNull(body); // body is never null here
        setOriginalInstance(request, problemDetail);
        problemDetail.setType(URI.create("about:blank"));
        return new ResponseEntity<>(body, headers, statusCode);
    }

    private static void setOriginalInstance(@NotNull WebRequest request, ProblemDetail problemDetail) {
        Map<String, @Nullable Object> errorAttributes = ERROR_ATTRIBUTES_PROVIDER.getErrorAttributes(
            request,
            ErrorAttributeOptions.defaults()
        );
        Object path = errorAttributes.get("path");
        if (path != null) {
            problemDetail.setInstance(URI.create(path.toString()));
        }
    }
}
