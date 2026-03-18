package com.linknest.backend.security.oauth2;

import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        // provider 식별
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> claims = new HashMap<>(oidcUser.getClaims());

        OAuth2UserInfo info = toUserInfo(registrationId, claims);

        // providerId 보장: OIDC subject 사용
        String providerId = getValidProviderId(info, oidcUser);

        User user = findOrCreateUser(info, providerId);
        Collection<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(user.getRole().name()));

        // 핸들러에서 사용하는 부가 속성
        claims.put("provider", info.getProvider().name());
        claims.put("providerId", providerId);
        claims.put("userId", user.getId());

        OidcUserInfo oidcUserInfo = new OidcUserInfo(claims);

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUserInfo, nameAttributeKey);
    }

    private OAuth2UserInfo toUserInfo(String registrationId, Map<String, Object> claims) {
        return switch (registrationId) {
            case "google" -> GoogleUserInfo.from(claims);
            case "kakao" -> KakaoUserInfo.from(claims);
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "Unsupported provider: " + registrationId
            );
        };
    }

    private String getValidProviderId(OAuth2UserInfo info, OidcUser oidcUser) {
        String id = info.getProviderId();
        if(id == null || id.isBlank()) id = oidcUser.getSubject();
        if(id == null || id.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_provider_id"),
                    "ProviderId missing (info.providerId and OIDC subject both blank)"
            );
        }
        return id;
    }

    private User findOrCreateUser(OAuth2UserInfo info, String providerId) {
        return userRepository.findByProviderAndProviderId(info.getProvider(), providerId)
                .map(user -> {
                    user.setProviderProfileImageUrl(info.getPicture());
                    return user;
                })
                .orElseGet(() -> userRepository.save(
                        User.oauthSignup(info.getEmail(), info.getName(), info.getPicture(),
                                info.getProvider(), providerId)
                ));
    }
}
