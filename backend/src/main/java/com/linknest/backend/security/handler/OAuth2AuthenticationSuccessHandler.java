package com.linknest.backend.security.handler;

import com.linknest.backend.auth.web.AuthService;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.frontend.oauth2-success-redirect}")
    private String successRedirectUri;

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authRequestRepository =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // AuthorizationRequest 복원
        OAuth2AuthorizationRequest authReq = authRequestRepository.removeAuthorizationRequest(request, response);
        String appState = authReq != null ? (String) authReq.getAttributes().get("returnTo") : null;

        // 사용자 조회
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = authToken.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();

        Long userId = (Long) attributes.get("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ResponseCookie rtCookie = authService.login(user);
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

        // 리다이렉트 대상 구성
        String target = successRedirectUri;
        if(appState != null && !appState.isBlank()) {
            target = successRedirectUri + "?state=" + appState;
        }

        response.sendRedirect(target);
    }
}
