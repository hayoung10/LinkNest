package com.linknest.backend.userpreferences;

import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.user.domain.AuthProvider;
import com.linknest.backend.userpreferences.domain.DefaultBookmarkSort;
import com.linknest.backend.userpreferences.domain.DefaultLayout;
import com.linknest.backend.userpreferences.dto.UserPreferencesRes;
import com.linknest.backend.userpreferences.dto.UserPreferencesUpdateReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {
    private static final Long USER_ID = 1L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-04-02T00:00:00Z");

    @Mock
    private UserPreferencesRepository preferencesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferencesMapper preferencesMapper;

    private UserPreferencesService userPreferencesService;

    @BeforeEach
    void setUp() {
        userPreferencesService = new UserPreferencesService(
                preferencesRepository,
                userRepository,
                preferencesMapper
        );
    }

    @Nested
    @DisplayName("get")
    class GetTest {
        @Test
        @DisplayName("저장된 사용자 설정을 조회한다")
        void get_returns_saved_preferences() {
            UserPreferences preferences = preferences(
                    10L,
                    user(USER_ID),
                    DefaultBookmarkSort.TITLE,
                    DefaultLayout.CARD,
                    false,
                    false
            );
            UserPreferencesRes expected = new UserPreferencesRes("TITLE", "CARD", false, false);

            when(preferencesRepository.findByUserId(USER_ID)).thenReturn(Optional.of(preferences));
            when(preferencesMapper.toRes(preferences)).thenReturn(expected);

            UserPreferencesRes result = userPreferencesService.get(USER_ID);

            assertThat(result).isSameAs(expected);
            verify(userRepository, never()).findById(any());
            verify(preferencesRepository, never()).save(any());
        }

        @Test
        @DisplayName("저장된 설정이 없으면 기본 설정을 만들어 반환한다")
        void get_returns_default_preferences_when_none_exist() {
            User user = user(USER_ID);
            UserPreferences saved = preferences(
                    20L,
                    user,
                    DefaultBookmarkSort.NEWEST,
                    DefaultLayout.LIST,
                    true,
                    true
            );
            UserPreferencesRes expected = new UserPreferencesRes("NEWEST", "LIST", true, true);

            when(preferencesRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(preferencesRepository.save(any(UserPreferences.class))).thenReturn(saved);
            when(preferencesMapper.toRes(saved)).thenReturn(expected);

            UserPreferencesRes result = userPreferencesService.get(USER_ID);

            assertThat(result).isSameAs(expected);

            ArgumentCaptor<UserPreferences> captor = ArgumentCaptor.forClass(UserPreferences.class);
            verify(preferencesRepository).save(captor.capture());

            UserPreferences created = captor.getValue();
            assertThat(created.getUser()).isSameAs(user);
            assertThat(created.getDefaultBookmarkSort()).isEqualTo(DefaultBookmarkSort.NEWEST);
            assertThat(created.getDefaultLayout()).isEqualTo(DefaultLayout.LIST);
            assertThat(created.isOpenInNewTab()).isTrue();
            assertThat(created.isKeepSignedIn()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 기본 설정 생성 시 예외가 발생한다")
        void get_throw_when_user_does_not_exist() {
            when(preferencesRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userPreferencesService.get(USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));

            verify(preferencesRepository, never()).save(any());
            verify(preferencesMapper, never()).toRes(any());
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {
        @Test
        @DisplayName("저장된 사용자 설정을 변경한다")
        void update_changes_saved_preferences() {
            UserPreferences preferences = preferences(
                    10L,
                    user(USER_ID),
                    DefaultBookmarkSort.NEWEST,
                    DefaultLayout.LIST,
                    true,
                    true
            );
            UserPreferencesUpdateReq req = new UserPreferencesUpdateReq(
                    "TITLE",
                    "CARD",
                    false,
                    false
            );
            UserPreferencesRes expected = new UserPreferencesRes("TITLE", "CARD", false, false);

            when(preferencesRepository.findByUserId(USER_ID)).thenReturn(Optional.of(preferences));
            doAnswer(invocation -> {
                UserPreferences target = invocation.getArgument(1);
                target.setDefaultBookmarkSort(DefaultBookmarkSort.TITLE);
                target.setDefaultLayout(DefaultLayout.CARD);
                target.setOpenInNewTab(false);
                target.setKeepSignedIn(false);
                return null;
            }).when(preferencesMapper).updateFromDto(eq(req), eq(preferences));
            when(preferencesMapper.toRes(preferences)).thenReturn(expected);

            UserPreferencesRes result = userPreferencesService.update(USER_ID, req);

            assertThat(result).isSameAs(expected);
            assertThat(preferences.getDefaultBookmarkSort()).isEqualTo(DefaultBookmarkSort.TITLE);
            assertThat(preferences.getDefaultLayout()).isEqualTo(DefaultLayout.CARD);
            assertThat(preferences.isOpenInNewTab()).isFalse();
            assertThat(preferences.isKeepSignedIn()).isFalse();

            verify(userRepository, never()).findById(any());
            verify(preferencesRepository, never()).save(any());
        }

        @Test
        @DisplayName("저장된 설정이 없으면 기본 설정을 만든 뒤 변경한다")
        void update_creates_default_preferences_before_changing_them() {
            User user = user(USER_ID);
            UserPreferences saved = preferences(
                    20L,
                    user,
                    DefaultBookmarkSort.NEWEST,
                    DefaultLayout.LIST,
                    true,
                    true
            );
            UserPreferencesUpdateReq req = new UserPreferencesUpdateReq(
                    "OLDEST",
                    "CARD",
                    false,
                    true
            );
            UserPreferencesRes expected = new UserPreferencesRes("OLDEST", "CARD", false, true);

            when(preferencesRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(preferencesRepository.save(any(UserPreferences.class))).thenReturn(saved);
            doAnswer(invocation -> {
                UserPreferences target = invocation.getArgument(1);
                target.setDefaultBookmarkSort(DefaultBookmarkSort.OLDEST);
                target.setDefaultLayout(DefaultLayout.CARD);
                target.setOpenInNewTab(false);
                target.setKeepSignedIn(true);
                return null;
            }).when(preferencesMapper).updateFromDto(eq(req), eq(saved));
            when(preferencesMapper.toRes(saved)).thenReturn(expected);

            UserPreferencesRes result = userPreferencesService.update(USER_ID, req);

            assertThat(result).isSameAs(expected);

            ArgumentCaptor<UserPreferences> captor = ArgumentCaptor.forClass(UserPreferences.class);
            verify(preferencesRepository).save(captor.capture());

            UserPreferences created = captor.getValue();
            assertThat(created.getUser()).isSameAs(user);
            assertThat(created.getDefaultBookmarkSort()).isEqualTo(DefaultBookmarkSort.NEWEST);
            assertThat(created.getDefaultLayout()).isEqualTo(DefaultLayout.LIST);
            assertThat(created.isOpenInNewTab()).isTrue();
            assertThat(created.isKeepSignedIn()).isTrue();

            assertThat(saved.getDefaultBookmarkSort()).isEqualTo(DefaultBookmarkSort.OLDEST);
            assertThat(saved.getDefaultLayout()).isEqualTo(DefaultLayout.CARD);
            assertThat(saved.isOpenInNewTab()).isFalse();
            assertThat(saved.isKeepSignedIn()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 설정을 변경하면 예외가 발생한다")
        void update_throw_when_user_does_not_exist() {
            UserPreferencesUpdateReq req = new UserPreferencesUpdateReq(
                    "TITLE",
                    "CARD",
                    false,
                    false
            );

            when(preferencesRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userPreferencesService.update(USER_ID, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));

            verify(preferencesRepository, never()).save(any());
            verify(preferencesMapper, never()).updateFromDto(any(), any());
        }
    }

    @Nested
    @DisplayName("getDefaultBookmarkSort")
    class GetDefaultBookmarkSortTest {
        @Test
        @DisplayName("저장된 기본 북마크 정렬을 반환한다")
        void get_default_bookmark_sort_returns_saved_sort() {
            UserPreferences preferences = preferences(
                    10L,
                    user(USER_ID),
                    DefaultBookmarkSort.TITLE,
                    DefaultLayout.LIST,
                    true,
                    true
            );

            when(preferencesRepository.findByUserId(USER_ID)).thenReturn(Optional.of(preferences));

            DefaultBookmarkSort result = userPreferencesService.getDefaultBookmarkSort(USER_ID);

            assertThat(result).isEqualTo(DefaultBookmarkSort.TITLE);
            verify(userRepository, never()).findById(any());
            verify(preferencesRepository, never()).save(any());
        }

        @Test
        @DisplayName("저장된 설정이 없으면 기본 북마크 정렬을 만들어 반환한다")
        void get_default_bookmark_sort_returns_default_sort_when_none_exist() {
            User user = user(USER_ID);
            UserPreferences saved = preferences(
                    20L,
                    user,
                    DefaultBookmarkSort.NEWEST,
                    DefaultLayout.LIST,
                    true,
                    true
            );

            when(preferencesRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(preferencesRepository.save(any(UserPreferences.class))).thenReturn(saved);

            DefaultBookmarkSort result = userPreferencesService.getDefaultBookmarkSort(USER_ID);

            assertThat(result).isEqualTo(DefaultBookmarkSort.NEWEST);

            ArgumentCaptor<UserPreferences> captor = ArgumentCaptor.forClass(UserPreferences.class);
            verify(preferencesRepository).save(captor.capture());

            UserPreferences created = captor.getValue();
            assertThat(created.getUser()).isSameAs(user);
            assertThat(created.getDefaultBookmarkSort()).isEqualTo(DefaultBookmarkSort.NEWEST);
            assertThat(created.getDefaultLayout()).isEqualTo(DefaultLayout.LIST);
            assertThat(created.isOpenInNewTab()).isTrue();
            assertThat(created.isKeepSignedIn()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 기본 북마크 정렬을 조회하면 예외가 발생한다")
        void get_default_bookmark_sort_throw_when_user_does_not_exist() {
            when(preferencesRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userPreferencesService.getDefaultBookmarkSort(USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));

            verify(preferencesRepository, never()).save(any());
        }
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("honggildong@example.com")
                .name("honggildong")
                .provider(AuthProvider.GOOGLE)
                .providerId("google-123")
                .role(User.Role.ROLE_USER)
                .createdAt(FIXED_INSTANT)
                .updatedAt(FIXED_INSTANT)
                .build();
    }

    private UserPreferences preferences(
            Long id,
            User user,
            DefaultBookmarkSort sort,
            DefaultLayout layout,
            boolean openInNewTab,
            boolean keepSignedIn
    ) {
        return UserPreferences.builder()
                .id(id)
                .user(user)
                .defaultBookmarkSort(sort)
                .defaultLayout(layout)
                .openInNewTab(openInNewTab)
                .keepSignedIn(keepSignedIn)
                .createdAt(FIXED_INSTANT)
                .updatedAt(FIXED_INSTANT)
                .build();
    }
}
