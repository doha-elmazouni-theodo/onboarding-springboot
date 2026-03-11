package com.theodo.springblueprint.common.api.security;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

import tools.jackson.databind.ObjectMapper;
import com.theodo.springblueprint.common.api.exceptionhandling.CommonErrorCodes;
import com.theodo.springblueprint.common.api.exceptionhandling.base.BaseExceptionHandler;
import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;

@Slf4j
@Component
public class AuthenticationProblemEntryPoint extends BaseExceptionHandler implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    protected AuthenticationProblemEntryPoint(EventPublisherPort eventPublisher, ObjectMapper mapper) {
        super(eventPublisher);
        this.mapper = mapper;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        if (response.isCommitted()) {
            log.warn("Response already committed.");
            return;
        }
        ServletWebRequest webRequest = new ServletWebRequest(request, response);

        ResponseEntity<Object> responseEntity = getDefaultResponseEntity(
            authException,
            webRequest,
            HttpStatus.UNAUTHORIZED,
            CommonErrorCodes.UNAUTHORIZED_ERROR
        );

        ProblemDetail problemDetail = (ProblemDetail) castNonNull(castNonNull(responseEntity).getBody());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        mapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
