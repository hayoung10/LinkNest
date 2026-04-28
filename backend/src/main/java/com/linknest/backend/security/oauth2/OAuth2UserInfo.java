package com.linknest.backend.security.oauth2;

import com.linknest.backend.user.domain.AuthProvider;

public interface OAuth2UserInfo {
    AuthProvider getProvider();
    String getProviderId();

    String getEmail();
    String getName();
    String getPicture();

    default boolean emailVerified() { return true; }
}
