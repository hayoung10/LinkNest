package com.linknest.backend.security.oauth2;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class OAuth2UserLoader {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public OAuth2User load(OAuth2UserRequest userRequest) {
        return delegate.loadUser(userRequest);
    }
}
