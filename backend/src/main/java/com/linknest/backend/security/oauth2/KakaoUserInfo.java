package com.linknest.backend.security.oauth2;

import com.linknest.backend.user.domain.AuthProvider;

import java.util.Map;
import java.util.Objects;

public record KakaoUserInfo(
        String sub,
        String email,
        String name,
        String picture,
        boolean emailVerified
) implements OAuth2UserInfo {
    public static KakaoUserInfo from(Map<String, Object> attributes) {
        boolean verified = Boolean.TRUE.equals(attributes.get("is_email_verified"));

        return new KakaoUserInfo(
                Objects.toString(attributes.get("sub"), null),
                Objects.toString(attributes.get("email"), null),
                Objects.toString(attributes.get("nickname"), ""),
                Objects.toString(attributes.get("picture"), ""),
                verified
        );
    }

    @Override
    public AuthProvider getProvider() { return AuthProvider.KAKAO; }

    @Override
    public String getProviderId() { return sub; }

    @Override
    public String getEmail() { return email; }

    @Override
    public String getName() { return name; }

    @Override
    public String getPicture() { return picture; }
}
