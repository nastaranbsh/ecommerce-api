package com.nbsh.commerceapi.common.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;

@Component
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public RestAccessDeniedHandler(
            JsonMapper jsonMapper
    ) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {

        response.setStatus(
                HttpStatus.FORBIDDEN.value()
        );

        response.setContentType(
                "application/json"
        );

        SecurityErrorResponse error =
                new SecurityErrorResponse(
                        Instant.now(),
                        HttpStatus.FORBIDDEN.value(),
                        HttpStatus.FORBIDDEN
                                .getReasonPhrase(),
                        "You do not have permission to perform this action",
                        request.getRequestURI()
                );

        jsonMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}