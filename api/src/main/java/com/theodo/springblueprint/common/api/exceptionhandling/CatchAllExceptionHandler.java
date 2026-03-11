package com.theodo.springblueprint.common.api.exceptionhandling;

import com.theodo.springblueprint.common.api.exceptionhandling.base.BaseExceptionHandler;
import com.theodo.springblueprint.common.api.exceptionhandling.base.ErrorCode;
import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class CatchAllExceptionHandler extends BaseExceptionHandler {

    protected CatchAllExceptionHandler(EventPublisherPort eventPublisher) {
        super(eventPublisher);
    }

    @ExceptionHandler({ Exception.class })
    @Nullable public final ResponseEntity<Object> catchAll(Exception ex, WebRequest request) {
        return getDefaultResponseEntity(
            ex,
            request,
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCodes.INTERNAL_SERVER_ERROR
        );
    }

    @Getter
    private enum ErrorCodes implements ErrorCode {
        INTERNAL_SERVER_ERROR("errors.internal_server_error");

        private final String code;

        ErrorCodes(String code) {
            this.code = code;
        }
    }
}
