package com.linknest.backend.auth.web;

import com.linknest.backend.auth.token.TokenService;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.common.utils.CookieUtils;
import com.linknest.backend.config.props.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequestMapping(API_PREFIX + "/auth")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    private static final String RT_COOKIE = "refresh_token";

    @PostMapping("/refresh")
    public ResponseEntity refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.getCookieValue(request, RT_COOKIE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // AT/RT 재발급
        Map<String, String> tokens = tokenService.rotateTokens(refreshToken);
        String newAt = tokens.get("accessToken");
        String newRt = tokens.get("refreshToken");

        // AT -> JSON
        Map<String, Object> body = Map.of(
                "accessToken", newAt,
                "tokenType", "Bearer",
                "expiresIn", jwtProperties.getAccessExpMinutes() * 60
        );

        // RT -> 쿠키
        int maxAge = (int) Duration.ofDays(jwtProperties.getRefreshExpDays()).toSeconds();
        ResponseCookie cookie = CookieUtils.createCookie(RT_COOKIE, newRt, maxAge);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 RT 추출
        String refreshToken = CookieUtils.getCookieValue(request, RT_COOKIE).orElse(null);
        if(refreshToken != null) {
            tokenService.revokeToken(refreshToken);
        }

        // 쿠키 삭제
        ResponseCookie delCookie = CookieUtils.deleteCookie(RT_COOKIE);
        response.addHeader(HttpHeaders.SET_COOKIE, delCookie.toString());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> logoutFromAllDevices(@AuthenticationPrincipal(expression = "id") Long userId,
                                                     HttpServletResponse response) {
        tokenService.revokeAllTokens(userId);

        // 현재 브라우저의 쿠키 삭제
        ResponseCookie delCookie = CookieUtils.deleteCookie(RT_COOKIE);
        response.addHeader(HttpHeaders.SET_COOKIE, delCookie.toString());

        return ResponseEntity.noContent().build();
    }
}
