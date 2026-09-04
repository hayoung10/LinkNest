package com.linknest.backend.auth.web;

import com.linknest.backend.auth.token.TokenService;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.config.props.JwtProperties;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.user.domain.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    private static final Long USER_ID = 1L;
    private static final String TEST_PROVIDER_ID = "test";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final int REFRESH_EXP_DAYS = 7;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtProperties jwtProperties;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                tokenService,
                jwtProperties
        );
    }

    @Nested
    @DisplayName("testLogin")
    class TestLoginTest {
        @Test
        @DisplayName("테스트 계정으로 로그인한다")
        void test_login_issues_refresh_token_cookie() {
            User testUser = testUser();

            when(userRepository.findByProviderAndProviderId(AuthProvider.TEST, TEST_PROVIDER_ID)).thenReturn(Optional.of(testUser));
            when(tokenService.issueTokens(USER_ID, "테스트 계정", List.of(User.Role.ROLE_USER.name())))
                    .thenReturn(Map.of("accessToken", "access-token", "refreshToken", REFRESH_TOKEN));
            when(jwtProperties.getRefreshExpDays()).thenReturn(REFRESH_EXP_DAYS);

            ResponseCookie result = authService.testLogin();

            assertThat(result.getName()).isEqualTo("refresh_token");
            assertThat(result.getValue()).isEqualTo(REFRESH_TOKEN);
            assertThat(result.getMaxAge()).isEqualTo(Duration.ofDays(REFRESH_EXP_DAYS));

            verify(userRepository).findByProviderAndProviderId(AuthProvider.TEST, TEST_PROVIDER_ID);
            verify(tokenService).issueTokens(USER_ID, "테스트 계정", List.of(User.Role.ROLE_USER.name()));
        }

        @Test
        @DisplayName("테스트 계정이 없으면 예외가 발생한다")
        void test_login_throw_when_test_user_does_not_exist() {
            when(userRepository.findByProviderAndProviderId(AuthProvider.TEST, TEST_PROVIDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.testLogin())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));

            verify(tokenService, never()).issueTokens(anyLong(), anyString(), anyList());
            verifyNoInteractions(jwtProperties);
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTest {
        @Test
        @DisplayName("Refresh Token을 발급하고 쿠키를 생성한다")
        void login_issues_refresh_token_cookie() {
            User user = user(USER_ID);

            when(tokenService.issueTokens(USER_ID, "honggildong", List.of(User.Role.ROLE_USER.name())))
                    .thenReturn(Map.of("accessToken", "access-token", "refreshToken", REFRESH_TOKEN));
            when(jwtProperties.getRefreshExpDays()).thenReturn(REFRESH_EXP_DAYS);

            ResponseCookie result = authService.login(user);

            assertThat(result.getName()).isEqualTo("refresh_token");
            assertThat(result.getValue()).isEqualTo(REFRESH_TOKEN);
            assertThat(result.getMaxAge()).isEqualTo(Duration.ofDays(REFRESH_EXP_DAYS));

            verify(tokenService).issueTokens(USER_ID, "honggildong", List.of(User.Role.ROLE_USER.name()));
            verify(jwtProperties).getRefreshExpDays();
        }
    }


    private User testUser() {
        return User.builder()
                .id(USER_ID)
                .email("test@linknest.com")
                .name("테스트 계정")
                .provider(AuthProvider.TEST)
                .providerId(TEST_PROVIDER_ID)
                .role(User.Role.ROLE_USER)
                .build();
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("honggildong@example.com")
                .name("honggildong")
                .provider(AuthProvider.GOOGLE)
                .providerId("google-123")
                .role(User.Role.ROLE_USER)
                .build();
    }
}
