package com.linknest.backend.common.config;

import com.linknest.backend.security.auth.PrincipalUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

@Configuration
public class AuditConfig {
    @Bean
    public AuditorAware<Long> auditorAware() {
        return() -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();
            if(principal instanceof PrincipalUser p) {
                return Optional.ofNullable(p.id());
            }
            return Optional.empty();
        };
    }
}
