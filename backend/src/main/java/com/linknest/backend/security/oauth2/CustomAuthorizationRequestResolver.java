package com.linknest.backend.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private static final String AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";
    private static final String ATTR_RETURN_TO = "returnTo";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, AUTHORIZATION_REQUEST_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = delegate.resolve(request);
        return customize(request, req);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = delegate.resolve(request, clientRegistrationId);
        return customize(request, req);
    }

    @Nullable
    private OAuth2AuthorizationRequest customize(HttpServletRequest request,
                                                 @Nullable OAuth2AuthorizationRequest original) {
        if(original == null) return null;

        String returnTo = Optional.ofNullable(request.getParameter(ATTR_RETURN_TO))
                .orElse(request.getParameter("state"));

        if(returnTo == null || returnTo.isBlank()) {
            return original;
        }

        // 기존 요청을 복제하면서 attributes에 returnTo 추가
        return OAuth2AuthorizationRequest.from(original)
                .attributes(attrs -> attrs.put(ATTR_RETURN_TO, returnTo))
                .build();
    }
}
