package com.linknest.backend.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linknest.backend.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// ErrorResponse를 JSON 응답으로 출력

public class ErrorResponder {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void send(HttpServletResponse response,
                             HttpStatus status,
                             ErrorResponse errorResponse) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    public static void send(HttpServletRequest request, HttpServletResponse response,
                             HttpStatus status, String code, String message) throws IOException {
        ErrorResponse error = ErrorResponse.of(
                status.value(),
                code,
                message,
                request.getRequestURI()
        );
        send(response, status, error);
    }
}
