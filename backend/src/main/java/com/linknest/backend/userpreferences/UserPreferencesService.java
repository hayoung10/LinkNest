package com.linknest.backend.userpreferences;

import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.userpreferences.domain.DefaultBookmarkSort;
import com.linknest.backend.userpreferences.domain.DefaultLayout;
import com.linknest.backend.userpreferences.dto.UserPreferencesRes;
import com.linknest.backend.userpreferences.dto.UserPreferencesUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPreferencesService {
    private final UserPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    private final UserPreferencesMapper mapper;

    @Transactional
    public UserPreferencesRes get(Long userId) {
        UserPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));
        return mapper.toRes(preferences);
    }

    @Transactional
    public UserPreferencesRes update(Long userId, UserPreferencesUpdateReq req) {
        UserPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));
        mapper.updateFromDto(req, preferences);
        return mapper.toRes(preferences);
    }

    public DefaultBookmarkSort getDefaultBookmarkSort(Long userId) {
        UserPreferences userPreferences = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));

        return userPreferences.getDefaultBookmarkSort();
    }

    private UserPreferences createDefault(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserPreferences preferences = UserPreferences.builder()
                .user(user)
                .defaultBookmarkSort(DefaultBookmarkSort.NEWEST)
                .defaultLayout(DefaultLayout.LIST)
                .openInNewTab(true)
                .keepSignedIn(true)
                .build();

        return preferencesRepository.save(preferences);
    }
}
