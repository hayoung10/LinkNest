package com.linknest.backend.config.security;

import com.linknest.backend.config.props.JwtProperties;
import com.linknest.backend.security.handler.OAuth2AuthenticationFailureHandler;
import com.linknest.backend.security.handler.OAuth2AuthenticationSuccessHandler;
import com.linknest.backend.security.handler.UserAccessDeniedHandler;
import com.linknest.backend.security.handler.UserAuthenticationEntryPoint;
import com.linknest.backend.security.jwt.JwtTokenizer;
import com.linknest.backend.security.jwt.JwtVerificationFilter;
import com.linknest.backend.security.oauth2.CustomAuthorizationRequestResolver;
import com.linknest.backend.security.oauth2.CustomOAuth2UserService;
import com.linknest.backend.security.oauth2.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final UserAccessDeniedHandler userAccessDeniedHandler;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;
    private final CustomOidcUserService customOidcUserService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
    private final JwtTokenizer jwtTokenizer;
    private final JwtProperties jwtProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(userAuthenticationEntryPoint)
                        .accessDeniedHandler(userAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout", "/api/v1/auth/sessions").authenticated()
                        .requestMatchers("/api/v1/users/**",
                                "/api/v1/bookmarks/**",
                                "/api/v1/collections/**",
                                "/api/v1/tags/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(a -> a
                                .authorizationRequestResolver(customAuthorizationRequestResolver))
                        .userInfoEndpoint(user -> user
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                );

        http.addFilterBefore(
                new JwtVerificationFilter(jwtTokenizer, jwtProperties),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}
