package com.linknest.backend.security.oauth2;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class OidcUserLoader {
    private final OidcUserService delegate = new OidcUserService();

    public OidcUser load(OidcUserRequest userRequest) {
        return delegate.loadUser(userRequest);
    }
}
