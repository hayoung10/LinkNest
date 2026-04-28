package com.linknest.backend.security.handler;

import com.linknest.backend.auth.token.TokenService;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.config.props.JwtProperties;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.user.domain.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {
    @Mock
    private TokenService tokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authRequestRepository =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                successHandler,
                "successRedirectUri",
                "http://localhost:5173/redirect"
        );
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 시 refresh_token 쿠키를 설정하고 기본 redirect URI로 이동한다")
    void onAuthenticationSuccess_redirects_with_cookie() throws IOException {
        Long userId = 1L;
        String username = "honggildong";
        String refreshToken = "new-refresh-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthenticationToken authentication = oauth2Authentication(userId);

        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name(username)
                .provider(AuthProvider.GOOGLE)
                .providerId("provider-id")
                .role(User.Role.ROLE_USER)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(tokenService.issueTokens(userId, username, List.of(User.Role.ROLE_USER.name())))
                .willReturn(Map.of(
                        "accessToken", "new-access-token",
                        "refreshToken", refreshToken
                ));
        given(jwtProperties.getRefreshExpDays()).willReturn(7);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        then(userRepository).should().findById(userId);
        then(tokenService).should().issueTokens(userId, username, List.of(User.Role.ROLE_USER.name()));

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("refresh_token=" + refreshToken);
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("SameSite=None");

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/redirect");
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 시 returnTo가 있으면 state 파라미터를 붙여 redirect 한다")
    void onAuthenticationSuccess_redirects_with_state() throws IOException {
        Long userId = 1L;
        String username = "honggildong";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("http://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .state("oauth-state")
                .authorizationRequestUri("https://accounts.google.com/o/oauth2/v2/auth?client_id=test-client-id")
                .attributes(Map.of("returnTo", "/workspace"))
                .build();

        authRequestRepository.saveAuthorizationRequest(authRequest, request, response);

        request.addParameter("state", "oauth-state");

        OAuth2AuthenticationToken authentication = oauth2Authentication(userId);

        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name(username)
                .provider(AuthProvider.GOOGLE)
                .providerId("provider-id")
                .role(User.Role.ROLE_USER)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(tokenService.issueTokens(userId, username, List.of(User.Role.ROLE_USER.name())))
                .willReturn(Map.of(
                        "accessToken", "new-access-token",
                        "refreshToken", "new-refresh-token"
                ));
        given(jwtProperties.getRefreshExpDays()).willReturn(7);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/redirect?state=/workspace");
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 처리 중 사용자가 없으면 USER_NOT_FOUND 예외가 발생한다")
    void onAuthenticationSuccess_throws_when_user_not_found() {
        Long userId = 999L;

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthenticationToken authentication = oauth2Authentication(userId);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> successHandler.onAuthenticationSuccess(request, response, authentication)
        );

        assertThat(exception.getErrorCode().name()).isEqualTo("USER_NOT_FOUND");
    }

    private OAuth2AuthenticationToken oauth2Authentication(Long userId) {
        OAuth2User principal = org.mockito.Mockito.mock(OAuth2User.class);
        given(principal.getAttributes()).willReturn(Map.of("userId", userId));

        OAuth2AuthenticationToken authentication = org.mockito.Mockito.mock(OAuth2AuthenticationToken.class);

        given(authentication.getPrincipal()).willReturn(principal);
        return authentication;
    }
}
