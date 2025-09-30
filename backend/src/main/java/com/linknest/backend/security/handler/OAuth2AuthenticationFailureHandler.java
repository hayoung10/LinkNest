package com.linknest.backend.security.handler;

import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.common.utils.ErrorResponder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ErrorResponder errorResponder;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        errorResponder.send(
                request, response,
                ErrorCode.OAUTH2_AUTHENTICATION_FAILED.getStatus(),
                ErrorCode.OAUTH2_AUTHENTICATION_FAILED.name(),
                ErrorCode.OAUTH2_AUTHENTICATION_FAILED.getMessage()
        );
    }
}
