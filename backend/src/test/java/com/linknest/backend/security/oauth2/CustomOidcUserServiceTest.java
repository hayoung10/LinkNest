package com.linknest.backend.security.oauth2;

import com.linknest.backend.user.ProviderProfileImageService;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.user.domain.AuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class CustomOidcUserServiceTest {
    @Mock
    private OidcUserLoader oidcUserLoader;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProviderProfileImageService providerProfileImageService;

    @InjectMocks
    private CustomOidcUserService customOidcUserService;

    @Test
    @DisplayName("기존 OIDC 사용자가 있으면 프로필 이미지를 동기화하고 반환한다")
    void loadUser_returns_existing_user() {
        OidcUserRequest userRequest = oidcUserRequest("google", "sub");
        OidcUser loadedUser = org.mockito.Mockito.mock(OidcUser.class);

        Map<String, Object> claims = Map.of(
                "sub", "google-123",
                "email", "test@example.com",
                "name", "honggildong",
                "picture", "https://image.example.com/profile.jpg"
        );

        OidcIdToken idToken = new OidcIdToken(
                "id-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                claims
        );

        User existingUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("honggildong")
                .provider(AuthProvider.GOOGLE)
                .providerId("google-123")
                .providerProfileImageUrl("old-image")
                .role(User.Role.ROLE_USER)
                .build();

        given(oidcUserLoader.load(userRequest)).willReturn(loadedUser);
        given(loadedUser.getClaims()).willReturn(claims);
        given(loadedUser.getIdToken()).willReturn(idToken);
        given(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-123"))
                .willReturn(Optional.of(existingUser));
        given(providerProfileImageService.syncProviderProfileImage(
                AuthProvider.GOOGLE,
                "google-123",
                "https://image.example.com/profile.jpg",
                "old-image"
        )).willReturn("new-image");

        OidcUser result = customOidcUserService.loadUser(userRequest);

        then(userRepository).should().findByProviderAndProviderId(AuthProvider.GOOGLE, "google-123");
        then(userRepository).shouldHaveNoMoreInteractions();
        then(providerProfileImageService).should().syncProviderProfileImage(
                AuthProvider.GOOGLE,
                "google-123",
                "https://image.example.com/profile.jpg",
                "old-image"
        );

        assertThat(result.getClaims().get("provider")).isEqualTo("GOOGLE");
        assertThat(result.getClaims().get("providerId")).isEqualTo("google-123");
        assertThat(result.getClaims().get("userId")).isEqualTo(1L);
    }

    @Test
    @DisplayName("기존 OIDC 사용자가 없으면 신규 사용자를 생성하고 반환한다")
    void loadUser_creates_new_user() {
        OidcUserRequest userRequest = oidcUserRequest("google", "sub");
        OidcUser loadedUser = org.mockito.Mockito.mock(OidcUser.class);

        Map<String, Object> claims = Map.of(
                "sub", "google-123",
                "email", "test@example.com",
                "name", "honggildong",
                "picture", "https://image.example.com/profile.jpg"
        );

        OidcIdToken idToken = new OidcIdToken(
                "id-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                claims
        );

        User savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("honggildong")
                .provider(AuthProvider.GOOGLE)
                .providerId("google-123")
                .providerProfileImageUrl("synced-image")
                .role(User.Role.ROLE_USER)
                .build();

        given(oidcUserLoader.load(userRequest)).willReturn(loadedUser);
        given(loadedUser.getClaims()).willReturn(claims);
        given(loadedUser.getIdToken()).willReturn(idToken);
        given(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-123"))
                .willReturn(Optional.empty());
        given(providerProfileImageService.syncProviderProfileImage(
                AuthProvider.GOOGLE,
                "google-123",
                "https://image.example.com/profile.jpg",
                null
        )).willReturn("synced-image");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        OidcUser result = customOidcUserService.loadUser(userRequest);

        then(userRepository).should().save(any(User.class));
        assertThat(result.getClaims().get("userId")).isEqualTo(1L);
    }

    @Test
    @DisplayName("지원하지 않는 OIDC provider면 예외가 발생한다")
    void loadUser_throws_when_provider_not_supported() {
        OidcUserRequest userRequest = oidcUserRequest("github", "sub");
        OidcUser loadedUser = org.mockito.Mockito.mock(OidcUser.class);

        Map<String, Object> claims = Map.of(
                "sub", "github-123"
        );

        given(oidcUserLoader.load(userRequest)).willReturn(loadedUser);
        given(loadedUser.getClaims()).willReturn(claims);

        assertThrows(OAuth2AuthenticationException.class,
                () -> customOidcUserService.loadUser(userRequest));
    }

    private OidcUserRequest oidcUserRequest(String registrationId, String userNameAttributeName) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(registrationId)
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://example.com/oauth2/authorize")
                .tokenUri("https://example.com/oauth2/token")
                .userInfoUri("https://example.com/userinfo")
                .userNameAttributeName(userNameAttributeName)
                .jwkSetUri("https://example.com/oauth2/jwks")
                .issuerUri("https://example.com")
                .clientName(registrationId)
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.now(),
                Instant.now().plusSeconds(300)
        );

        OidcIdToken idToken = new OidcIdToken(
                "id-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of(IdTokenClaimNames.SUB, "test-sub")
        );

        return new OidcUserRequest(clientRegistration, accessToken, idToken);
    }
}
