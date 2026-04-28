package com.linknest.backend.auth.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.linknest.backend.auth.token.TokenService;
import com.linknest.backend.config.props.JwtProperties;
import com.linknest.backend.storage.StorageProperties;
import com.linknest.backend.userpreferences.UserPreferencesService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

@WebMvcTest(TokenController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TokenControllerTest.TestClockConfig.class)
class TokenControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private UserPreferencesService userPreferencesService;

    @MockitoBean
    private StorageProperties storageProperties;

    private static final String API_PREFIX = "/api/v1";
    private static final String RT_COOKIE = "refresh_token";

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(
                    Instant.parse("2026-03-26T00:00:00Z"),
                    ZoneId.of("Asia/Seoul")
            );
        }
    }

    @Test
    @DisplayName("refresh 성공 - keepSignedIn=true이면 refresh_token 쿠키와 access token을 재발급한다")
    void refresh_success_keepSignedIn_true() throws Exception {
        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        String newAccessToken = "new-access-token";
        Long userId = 1L;

        given(tokenService.rotateTokens(oldRefreshToken))
                .willReturn(Map.of(
                        "accessToken", newAccessToken,
                        "refreshToken", newRefreshToken
                ));
        given(tokenService.getUserIdFromRefresh(oldRefreshToken)).willReturn(userId);
        given(jwtProperties.getRefreshExpDays()).willReturn(7);
        given(jwtProperties.getAccessExpMinutes()).willReturn(15);

        var preferences = Mockito.mock(com.linknest.backend.userpreferences.dto.UserPreferencesRes.class);
        given(preferences.keepSignedIn()).willReturn(true);
        given(userPreferencesService.get(userId)).willReturn(preferences);

        mockMvc.perform(
                post(API_PREFIX + "/auth/refresh")
                        .cookie(new Cookie(RT_COOKIE, oldRefreshToken))
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString(RT_COOKIE + "=" + newRefreshToken)))
                .andExpect(jsonPath("$.message").value("액세스 토큰 재발급"))
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(900));
    }

    @Test
    @DisplayName("refresh 실패 - refresh_token 쿠키가 없으면 401을 반환한다")
    void refresh_fail_when_cookie_missing() throws Exception {
        mockMvc.perform(post(API_PREFIX + "/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        then(tokenService).should(never()).rotateTokens(anyString());
    }

    @Test
    @DisplayName("logout 성공 - refresh_token 쿠키가 있으면 revoke 후 삭제 쿠키를 내려준다")
    void logout_success_with_cookie() throws Exception {
        String refreshToken = "refresh-token";

        mockMvc.perform(
                post(API_PREFIX + "/auth/logout")
                        .cookie(new Cookie(RT_COOKIE, refreshToken))
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString(RT_COOKIE + "=")))
                .andExpect(jsonPath("$.message").value("로그아웃 완료"));

        then(tokenService).should().revokeToken(eq(refreshToken));
    }

    @Test
    @DisplayName("logout 성공 - refresh_token 쿠키가 없어도 정상 응답과 삭제 쿠키를 내려준다")
    void logout_success_without_cookie() throws Exception {
        mockMvc.perform(post(API_PREFIX + "/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.message").value("로그아웃 완료"));

        then(tokenService).should(never()).revokeToken(anyString());
    }
}
