package com.linknest.backend.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linknest.backend.auth.token.TokenService;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.common.utils.CookieUtils;
import com.linknest.backend.config.props.JwtProperties;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper om;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();

        String provider = authToken.getAuthorizedClientRegistrationId();
        String providerId = (String) attributes.get("providerId");
        Long userId = (Long) attributes.get("userId");

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String username = user.getName();
        List<String> roles = List.of(user.getRole().name());

        // AT/RT 발급
        Map<String, String> tokens = tokenService.issueTokens(userId, username, roles);
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        // Access Token -> 헤더
        response.setHeader(jwtProperties.getHeader(), jwtProperties.getPrefix() + " " + accessToken);

        // Refresh Token -> HttpOnly 쿠키
        int maxAge = (int) Duration.ofDays(jwtProperties.getRefreshExpDays()).toSeconds();
        ResponseCookie rtCookie = CookieUtils.createCookie("refresh_token", refreshToken, maxAge);
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

        // 응답 (임시 JSON, 추후 리다이렉트로 교체)
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.OK.value());
        body.put("code", "OAUTH2_LOGIN_SUCCESS");
        body.put("provider", provider);
        body.put("providerId", providerId);
        body.put("userId", userId);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        om.writeValue(response.getWriter(), body);
    }
}
