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
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {
    @Mock
    private OAuth2UserLoader oAuth2UserLoader;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProviderProfileImageService providerProfileImageService;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("기존 OAuth2 사용자가 있으면 프로필 이미지를 동기화하고 반환한다")
    void loadUser_returns_existing_user() {
        OAuth2UserRequest userRequest = oauth2UserRequest("google", "sub");
        OAuth2User loadedUser = org.mockito.Mockito.mock(OAuth2User.class);

        Map<String, Object> attributes = Map.of(
                "sub", "google-123",
                "email", "test@example.com",
                "name", "honggildong",
                "picture", "https://image.example.com/profile.jpg"
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

        given(oAuth2UserLoader.load(userRequest)).willReturn(loadedUser);
        given(loadedUser.getAttributes()).willReturn(attributes);
        given(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-123"))
                .willReturn(Optional.of(existingUser));
        given(providerProfileImageService.syncProviderProfileImage(
                AuthProvider.GOOGLE,
                "google-123",
                "https://image.example.com/profile.jpg",
                "old-image"
        )).willReturn("new-image");

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        then(userRepository).should().findByProviderAndProviderId(AuthProvider.GOOGLE, "google-123");
        then(userRepository).shouldHaveNoMoreInteractions();
        then(providerProfileImageService).should().syncProviderProfileImage(
                AuthProvider.GOOGLE,
                "google-123",
                "https://image.example.com/profile.jpg",
                "old-image"
        );

        assertThat(result.getAttributes().get("provider")).isEqualTo("GOOGLE");
        assertThat(result.getAttributes().get("providerId")).isEqualTo("google-123");
        assertThat(result.getAttributes().get("userId")).isEqualTo(1L);
    }

    @Test
    @DisplayName("기존 OAuth2 사용자가 없으면 신규 사용자를 생성하고 반환한다")
    void loadUser_creates_new_user() {
        OAuth2UserRequest userRequest = oauth2UserRequest("google", "sub");
        OAuth2User loadedUser = org.mockito.Mockito.mock(OAuth2User.class);

        Map<String, Object> attributes = Map.of(
                "sub", "google-123",
                "email", "test@example.com",
                "name", "honggildong",
                "picture", "https://image.example.com/profile.jpg"
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

        given(oAuth2UserLoader.load(userRequest)).willReturn(loadedUser);
        given(loadedUser.getAttributes()).willReturn(attributes);
        given(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-123"))
                .willReturn(Optional.empty());
        given(providerProfileImageService.syncProviderProfileImage(
                AuthProvider.GOOGLE,
                "google-123",
                "https://image.example.com/profile.jpg",
                null
        )).willReturn("synced-image");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        then(userRepository).should().save(any(User.class));
        assertThat(result.getAttributes().get("userId")).isEqualTo(1L);
    }

    @Test
    @DisplayName("지원하지 않는 OAuth2 provider면 예외가 발생한다")
    void loadUser_throws_when_provider_not_supported() {
        OAuth2UserRequest userRequest = oauth2UserRequest("github", "id");
        OAuth2User loadedUser = org.mockito.Mockito.mock(OAuth2User.class);

        given(oAuth2UserLoader.load(userRequest)).willReturn(loadedUser);
        given(loadedUser.getAttributes()).willReturn(Map.of("id", "github-1"));

        assertThrows(OAuth2AuthenticationException.class,
                () -> customOAuth2UserService.loadUser(userRequest));
    }

    private OAuth2UserRequest oauth2UserRequest(String registrationId, String userNameAttributeName) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(registrationId)
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("profile", "email")
                .authorizationUri("https://example.com/oauth2/authorize")
                .tokenUri("https://example.com/oauth2/token")
                .userInfoUri("https://example.com/userinfo")
                .userNameAttributeName(userNameAttributeName)
                .clientName(registrationId)
                .build();

        return new OAuth2UserRequest(
                clientRegistration,
                new org.springframework.security.oauth2.core.OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        "access-token",
                        java.time.Instant.now(),
                        java.time.Instant.now().plusSeconds(300)
                )
        );
    }
}
