package com.linknest.backend.security.oauth2;

import com.linknest.backend.user.domain.AuthProvider;

import java.util.Map;

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
                (String) attributes.get("sub"),
                (String) attributes.get("email"),
                (String) attributes.getOrDefault("name", ""),
                (String) attributes.getOrDefault("picture", ""),
                verified
        );
    }

    @Override public AuthProvider getProvider() { return AuthProvider.GOOGLE; }
    @Override public String getProviderId() { return sub; }
    @Override public String getEmail() { return email; }
    @Override public String getName() { return name; }
    @Override public String getPicture() { return picture; }
}
