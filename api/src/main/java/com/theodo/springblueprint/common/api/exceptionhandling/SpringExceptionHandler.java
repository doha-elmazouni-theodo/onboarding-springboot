package com.theodo.springblueprint.common.api.exceptionhandling;

import tools.jackson.databind.exc.MismatchedInputException;
import com.theodo.springblueprint.common.api.exceptionhandling.base.BaseResponseEntityExceptionHandler;
import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;

import java.net.URI;
import java.util.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringExceptionHandler extends BaseResponseEntityExceptionHandler {

    protected SpringExceptionHandler(EventPublisherPort eventPublisher) {
        super(eventPublisher);
    }

    @ExceptionHandler({ AccessDeniedException.class })
    @Nullable public final ResponseEntity<Object> handleException(AccessDeniedException ex, WebRequest request) {
        if (request.getUserPrincipal() == null) {
            return getDefaultResponseEntity(ex, request, HttpStatus.UNAUTHORIZED, CommonErrorCodes.UNAUTHORIZED_ERROR);
        }
        return getDefaultResponseEntity(ex, request, HttpStatus.FORBIDDEN, SpringErrorCodes.ACCESS_DENIED_ERROR);
    }

    @Override
    @Nullable protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode status,
        @NotNull WebRequest request) {
        ProblemDetail problemDetail = ex.getBody();
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(SpringErrorCodes.INPUT_VALIDATION_ERROR.code());
        problemDetail.setDetail(SpringErrorCodes.INPUT_VALIDATION_ERROR.code());
        problemDetail.setProperty("errorMetadata", getViolationsMap(ex.getBindingResult()));
        return this.handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @Override
    @Nullable protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode status,
        @NotNull WebRequest request) {
        if (ex.getCause() instanceof MismatchedInputException) {
            ProblemDetail body = this
                .createProblemDetail(ex, status, SpringErrorCodes.INPUT_FORMAT_ERROR.code(), null, null, request);
            body.setTitle(SpringErrorCodes.INPUT_FORMAT_ERROR.code());
            return this.handleExceptionInternal(ex, body, headers, status, request);
        }
        if (Objects.toString(ex).contains("Required request body is missing")) {
            ProblemDetail body = this
                .createProblemDetail(ex, status, SpringErrorCodes.MISSING_BODY_ERROR.code(), null, null, request);
            body.setTitle(SpringErrorCodes.MISSING_BODY_ERROR.code());
            return this.handleExceptionInternal(ex, body, headers, status, request);
        }
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    private Map<String, Object> getViolationsMap(BindingResult bindingResult) {
        List<Map<String, Object>> fieldErrors = new ArrayList<>();
        List<Map<String, Object>> objectErrors = new ArrayList<>();
        for (ObjectError error : bindingResult.getAllErrors()) {
            if (error instanceof FieldError fieldError) {
                HashMap<String, Object> errorMap = new HashMap<>();
                errorMap.put("field", fieldError.getField());
                errorMap.put("constraint", fieldError.getCode());
                errorMap.put("rejectedValue", fieldError.getRejectedValue());
                errorMap.put("message", fieldError.getDefaultMessage());
                fieldErrors.add(errorMap);
            } else {
                HashMap<String, Object> errorMap = new HashMap<>();
                errorMap.put("constraint", error.getCode());
                errorMap.put("message", error.getDefaultMessage());
                objectErrors.add(errorMap);
            }
        }
        Map<String, Object> metadata = new HashMap<>();
        if (!objectErrors.isEmpty()) {
            metadata.put("errors", objectErrors);
        }
        if (!fieldErrors.isEmpty()) {
            metadata.put("fields", fieldErrors);
        }
        return metadata;
    }
}
