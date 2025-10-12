package com.linknest.backend.security.jwt;

import com.linknest.backend.config.props.JwtProperties;
import com.linknest.backend.security.auth.PrincipalUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final JwtProperties jwtProperties;

    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/login/oauth2/**",
            "/oauth2/**",
            "/actuator/**",
            "/swagger-ui/**", "/v3/api-docs/**",
            "/favicon.ico", "/error"
    );
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        for(String p: EXCLUDE_PATTERNS) {
            if(PATH_MATCHER.match(p, path)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveBearer(request);
            if(token != null) {
                JwtTokenizer.AtClaims atClaims = jwtTokenizer.parseAndValidateAccess(token);

                List<SimpleGrantedAuthority> authorities =
                        atClaims.roles().stream()
                                .filter(Objects::nonNull)
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                PrincipalUser principal = new PrincipalUser(
                        atClaims.userId(),
                        atClaims.username(),
                        atClaims.roles()
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBearer(HttpServletRequest request) {
        String headerName = jwtProperties.getHeader();
        String prefix = jwtProperties.getPrefix();
        String value = request.getHeader(headerName);
        if(value == null) return null;
        if(value.startsWith(prefix + " ")) {
            return value.substring((prefix + " ").length());
        }
        return null;
    }
}
