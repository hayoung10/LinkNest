package com.linknest.backend.config.jpa;

import com.linknest.backend.security.auth.PrincipalUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditConfig {
    @Bean
    public AuditorAware<Long> auditorAware() {
        return() -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
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
