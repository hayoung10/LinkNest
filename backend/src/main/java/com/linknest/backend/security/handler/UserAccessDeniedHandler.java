package com.linknest.backend.security.handler;

import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.common.utils.ErrorResponder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 권한 없는 요청을
// 인증은 되었지만 권한이 없는 요청을 403 Forbidden으로 처리

@Component
@RequiredArgsConstructor
public class UserAccessDeniedHandler implements AccessDeniedHandler {
    private final ErrorResponder errorResponder;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        errorResponder.send(
                request, response,
                ErrorCode.ACCESS_DENIED.getStatus(),
                ErrorCode.ACCESS_DENIED.name(),
                ErrorCode.ACCESS_DENIED.getMessage()
        );
    }
}
