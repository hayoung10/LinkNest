package com.linknest.backend.auth.web;

import com.linknest.backend.auth.token.TokenService;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.common.utils.CookieUtils;
import com.linknest.backend.config.props.JwtProperties;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.user.domain.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    public ResponseCookie testLogin() {
        User testUser = userRepository
                .findByProviderAndProviderId(AuthProvider.TEST, "test")
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return login(testUser);
    }

    public ResponseCookie login(User user) {
        String username = user.getName();
        List<String> roles = List.of(user.getRole().name());

        // RT 발급 및 쿠키 생성
        Map<String, String> tokens = tokenService.issueTokens(user.getId(), username, roles);

        // String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        int maxAge = (int) Duration.ofDays(jwtProperties.getRefreshExpDays()).toSeconds();

        return CookieUtils.createCookie("refresh_token", refreshToken, maxAge);
    }
}
