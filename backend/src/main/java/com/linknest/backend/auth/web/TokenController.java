package com.linknest.backend.auth.web;

import com.linknest.backend.auth.token.TokenService;
import com.linknest.backend.auth.web.dto.TokenRefreshRes;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.common.response.ApiResponse;
import com.linknest.backend.common.utils.CookieUtils;
import com.linknest.backend.config.props.JwtProperties;
import com.linknest.backend.userpreferences.UserPreferencesService;
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
    private final UserPreferencesService userPreferencesService;

    private static final String RT_COOKIE = "refresh_token";

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshRes>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.getCookieValue(request, RT_COOKIE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // AT/RT 재발급
        Map<String, String> tokens = tokenService.rotateTokens(refreshToken);
        String newAt = tokens.get("accessToken");
        String newRt = tokens.get("refreshToken");

        Long userId = tokenService.getUserIdFromRefresh(refreshToken);
        var preferences = userPreferencesService.get(userId);

        // RT -> 쿠키
        ResponseCookie cookie;
        if(preferences.keepSignedIn()) {
            int maxAge = (int) Duration.ofDays(jwtProperties.getRefreshExpDays()).toSeconds();
            cookie = CookieUtils.createCookie(RT_COOKIE, newRt, maxAge);
        } else {
            cookie = CookieUtils.createSessionCookie(RT_COOKIE, newRt); // 세션 쿠키
        }
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // AT -> JSON
        TokenRefreshRes body = new TokenRefreshRes(
                newAt,
                "Bearer",
                jwtProperties.getAccessExpMinutes() * 60
        );

        return ResponseEntity.ok(ApiResponse.ok("액세스 토큰 재발급", body));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 RT 추출
        String refreshToken = CookieUtils.getCookieValue(request, RT_COOKIE).orElse(null);
        if(refreshToken != null) {
            tokenService.revokeToken(refreshToken);
        }

        // 쿠키 삭제
        ResponseCookie delCookie = CookieUtils.deleteCookie(RT_COOKIE);
        response.addHeader(HttpHeaders.SET_COOKIE, delCookie.toString());

        return ResponseEntity.ok(ApiResponse.ok("로그아웃 완료"));
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<ApiResponse<Void>> logoutFromAllDevices(@AuthenticationPrincipal(expression = "id") Long userId,
                                                     HttpServletResponse response) {
        tokenService.revokeAllTokens(userId);

        // 현재 브라우저의 쿠키 삭제
        ResponseCookie delCookie = CookieUtils.deleteCookie(RT_COOKIE);
        response.addHeader(HttpHeaders.SET_COOKIE, delCookie.toString());

        return ResponseEntity.ok(ApiResponse.ok("모든 세션 로그아웃 완료"));
    }
}
