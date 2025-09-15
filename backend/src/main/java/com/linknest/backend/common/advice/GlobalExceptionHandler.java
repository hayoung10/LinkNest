package com.linknest.backend.common.advice;

import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bean Validation: @Valid 본문 검증 실패 (DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidException(MethodArgumentNotValidException e,
                                                              HttpServletRequest request) {
        log.warn("Validation: {}", e.getMessage());
        return buildValidationResponse(e.getBindingResult(), request, ErrorCode.INVALID_INPUT_VALUE);
    }

    // 쿼리파라미터/경로변수 타임 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                            HttpServletRequest request) {
        log.warn("Type mismatch: {}", e.getMessage());
        ErrorResponse.FieldError error = new ErrorResponse.FieldError(
                e.getName(),
                e.getValue() == null ? "" : String.valueOf(e.getValue()),
                "타입이 올바르지 않습니다."
        );
        ErrorResponse body = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ErrorCode.INVALID_INPUT_VALUE.name(),
                ErrorCode.INVALID_INPUT_VALUE.getDefaultMessage(),
                request.getRequestURI(),
                List.of(error)
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(body);
    }

    // 필수 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e,
                                                            HttpServletRequest request) {
        log.warn("Missing request parameter: {}", e.getParameterName());
        ErrorResponse.FieldError error = new ErrorResponse.FieldError(
                e.getParameterName(), "", "필수 파라미터가 누락되었습니다."
        );
        ErrorResponse body = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ErrorCode.INVALID_INPUT_VALUE.name(),
                ErrorCode.INVALID_INPUT_VALUE.getDefaultMessage(),
                request.getRequestURI(),
                List.of(error)
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(body);
    }

    // 지원하지 않는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e,
                                                                HttpServletRequest request) {
        log.error("Unhandled exception");
        return buildSimple(ErrorCode.METHOD_NOT_ALLOWED, request);
    }

    // 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e,
                                                        HttpServletRequest request) {
        ErrorCode code = e.getErrorCode();
        log.warn("Business error [{}]: {}", code.name(), e.getMessage());
        ErrorResponse body = ErrorResponse.of(
                code.getStatus().value(),
                code.name(),
                e.getMessage(),                 // 커스텀 메시지 허용
                request.getRequestURI()
        );
        return ResponseEntity.status(code.getStatus()).body(body);
    }

    // 예상 못한 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e,
                                                         HttpServletRequest request) {
        log.error("Unhandled exception", e);
        ErrorResponse body = ErrorResponse.of(
                ErrorCode.INTERNAL_ERROR.getStatus().value(),
                ErrorCode.INTERNAL_ERROR.name(),
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus()).body(body);
    }

    // ---- Helpers ----
    private ResponseEntity<ErrorResponse> buildValidationResponse(BindingResult bindingResult,
                                                                  HttpServletRequest request,
                                                                  ErrorCode code) {
        List<ErrorResponse.FieldError> errors = bindingResult.getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        java.util.Objects.toString(fe.getRejectedValue(), ""),
                        fe.getDefaultMessage()
                ))
                .toList();
        ErrorResponse body = ErrorResponse.of(
                code.getStatus().value(),
                code.name(),
                code.getDefaultMessage(),
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(code.getStatus()).body(body);
    }

    private ErrorResponse.FieldError toFieldError(FieldError fe) {
        String rejected = fe.getRejectedValue() == null ? "" : String.valueOf(fe.getRejectedValue());
        return new ErrorResponse.FieldError(fe.getField(), rejected, fe.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> buildSimple(ErrorCode code, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                code.getStatus().value(), code.name(), code.getDefaultMessage(), request.getRequestURI()
        );
        return ResponseEntity.status(code.getStatus()).body(body);
    }
}
