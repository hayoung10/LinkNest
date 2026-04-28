package com.linknest.backend.security.oauth2;

import com.linknest.backend.user.domain.AuthProvider;

import java.util.Map;
import java.util.Objects;

public record GoogleUserInfo(
        String sub,
        String email,
        String name,
        String picture,
        boolean emailVerified
) implements OAuth2UserInfo {
    public static GoogleUserInfo from(Map<String, Object> attributes) {
        boolean verified = Boolean.TRUE.equals(attributes.get("email_verified"));

        return new GoogleUserInfo(
                Objects.toString(attributes.get("sub"), null),
                Objects.toString(attributes.get("email"), null),
                Objects.toString(attributes.get("name"), ""),
                Objects.toString(attributes.get("picture"), ""),
                verified
        );
    }

    @Override public AuthProvider getProvider() { return AuthProvider.GOOGLE; }
    @Override public String getProviderId() { return sub; }
    @Override public String getEmail() { return email; }
    @Override public String getName() { return name; }
    @Override public String getPicture() { return picture; }
}
