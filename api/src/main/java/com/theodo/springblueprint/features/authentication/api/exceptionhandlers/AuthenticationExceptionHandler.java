package com.theodo.springblueprint.features.authentication.api.exceptionhandlers;

import com.theodo.springblueprint.common.api.exceptionhandling.CommonErrorCodes;
import com.theodo.springblueprint.common.api.exceptionhandling.base.BaseExceptionHandler;
import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;
import com.theodo.springblueprint.features.authentication.domain.exceptions.AbstractAuthenticationDomainException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class AuthenticationExceptionHandler extends BaseExceptionHandler {

    protected AuthenticationExceptionHandler(EventPublisherPort eventPublisher) {
        super(eventPublisher);
    }

    @ExceptionHandler({ AbstractAuthenticationDomainException.class })
    @Nullable public final ResponseEntity<Object> handleException(AbstractAuthenticationDomainException ex, WebRequest request) {
        return getDefaultResponseEntity(ex, request, HttpStatus.UNAUTHORIZED, CommonErrorCodes.UNAUTHORIZED_ERROR);
    }
}
