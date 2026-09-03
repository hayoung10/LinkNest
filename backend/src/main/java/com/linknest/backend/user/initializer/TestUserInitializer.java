package com.linknest.backend.user.initializer;

import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.user.domain.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestUserInitializer {
    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        if(userRepository
                .findByProviderAndProviderId(AuthProvider.TEST, "test")
                .isEmpty()) {
            User testUser = User.builder()
                    .email("test@linknest.com")
                    .name("테스트 계정")
                    .provider(AuthProvider.TEST)
                    .providerId("test")
                    .role(User.Role.ROLE_USER)
                    .build();

            userRepository.save(testUser);
        }
    }
}
