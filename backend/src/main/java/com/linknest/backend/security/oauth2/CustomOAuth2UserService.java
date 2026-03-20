package com.linknest.backend.security.oauth2;

import com.linknest.backend.user.ProviderProfileImageService;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final ProviderProfileImageService providerProfileImageService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 원본 사용자 정보 조회
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // provider 식별
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        OAuth2UserInfo info = toUserInfo(registrationId, attributes);

        // TODO: 이메일 검증 기능 구현 후 주석 해제
//        if(!info.emailVerified()) {
//            throw new OAuth2AuthenticationException(
//                    new OAuth2Error("email_not_verified"),
//                    "Email is not verified"
//            );
//        }

        String providerId = getValidProviderId(info, oAuth2User);

        // 기존 User가 존재하면 갱신, 없으면 새로 생성
        User user = findOrCreateUser(info, providerId);
        Collection<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(user.getRole().name()));

        // 핸들러에서 사용하는 부가 속성
        attributes.put("provider", info.getProvider().name());
        attributes.put("providerId", providerId);
        attributes.put("userId", user.getId());

        return new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
    }

    private OAuth2UserInfo toUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> GoogleUserInfo.from(attributes);
            case "kakao" -> KakaoUserInfo.from(attributes);
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "Unsupported provider: " + registrationId
            );
        };
    }

    private String getValidProviderId(OAuth2UserInfo info, OAuth2User oAuth2User) {
        String id = info.getProviderId();
        if(id == null || id.isBlank()) id = oAuth2User.getName();
        if(id == null || id.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_provider_id"),
                    "ProviderId missing (info.providerId and OAuth2User.getName() both blank)"
            );
        }
        return id;
    }

    private User findOrCreateUser(OAuth2UserInfo info, String providerId) {
        return userRepository.findByProviderAndProviderId(info.getProvider(), providerId)
                .map(user -> {
                    String syncedUrl = providerProfileImageService.syncProviderProfileImage(
                            info.getProvider(),
                            providerId,
                            info.getPicture(),
                            user.getProviderProfileImageUrl()
                    );
                    user.setProviderProfileImageUrl(syncedUrl);
                    return user;
                })
                .orElseGet(() -> {
                    String syncedUrl = providerProfileImageService.syncProviderProfileImage(
                            info.getProvider(),
                            providerId,
                            info.getPicture(),
                            null
                    );

                    User user = User.oauthSignup(info.getEmail(), info.getName(), syncedUrl,
                            info.getProvider(), providerId);
                    return userRepository.save(user);
                });
    }
}
