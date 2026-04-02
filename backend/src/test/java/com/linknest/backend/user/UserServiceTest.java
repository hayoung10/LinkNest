package com.linknest.backend.user;

import com.linknest.backend.auth.token.TokenService;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.storage.Storage;
import com.linknest.backend.user.domain.AuthProvider;
import com.linknest.backend.user.dto.UserRes;
import com.linknest.backend.user.dto.UserUpdateReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final Long USER_ID = 1L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-04-02T00:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Storage storage;

    @Mock
    private TokenService tokenService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                userMapper,
                storage,
                tokenService
        );
    }

    @Nested
    @DisplayName("get")
    class GetTest {
        @Test
        @DisplayName("사용자 정보를 조회한다")
        void get_returns_user_profile() {
            User user = user(USER_ID);
            UserRes expected = userRes(USER_ID, "honggildong@example.com", "honggildong", null, false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userMapper.toRes(user)).thenReturn(expected);

            UserRes result = userService.get(USER_ID);

            assertThat(result).isSameAs(expected);
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 조회하면 예외가 발생한다")
        void get_throw_when_user_does_not_exist() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.get(USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));

            verify(userMapper, never()).toRes(any());
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {
        @Test
        @DisplayName("사용자 정보를 수정한다")
        void update_changes_user_profile() {
            User user = user(USER_ID);
            UserUpdateReq req = new UserUpdateReq("Updated Name");
            UserRes expected = userRes(USER_ID, "honggildong@example.com", "Updated Name", null, false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            doAnswer(invocation -> {
                User target = invocation.getArgument(1);
                target.setName("Updated Name");
                return null;
            }).when(userMapper).updateFromDto(eq(req), eq(user));
            when(userMapper.toRes(user)).thenReturn(expected);

            UserRes result = userService.update(USER_ID, req);

            assertThat(result).isSameAs(expected);
            assertThat(user.getName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 정보를 수정하면 예외가 발생한다")
        void update_throw_when_user_does_not_exist() {
            UserUpdateReq req = new UserUpdateReq("Updated Name");

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.update(USER_ID, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));

            verify(userMapper, never()).updateFromDto(any(), any());
            verify(userMapper, never()).toRes(any());
        }
    }

    @Nested
    @DisplayName("updateProfileImage")
    class UpdateProfileImageTest {
        @Test
        @DisplayName("새 프로필 이미지를 등록한다")
        void update_profile_image_replaces_profile_image() {
            User user = user(USER_ID);
            MockMultipartFile file = new MockMultipartFile(
                    "profile",
                    "profile.png",
                    "image/png",
                    "hello".getBytes()
            );
            UserRes expected = userRes(
                    USER_ID,
                    "honggildong@example.com",
                    "honggildong",
                    "https://cdn.example.com/new-profile.png",
                    true
            );

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(storage.upload("profiles", file)).thenReturn("https://cdn.example.com/new-profile.png");
            when(userMapper.toRes(user)).thenReturn(expected);

            UserRes result = userService.updateProfileImage(USER_ID, file);

            assertThat(result).isSameAs(expected);
            assertThat(user.getProfileImageUrl()).isEqualTo("https://cdn.example.com/new-profile.png");

            verify(storage, never()).delete(any());
            verify(storage).upload("profiles", file);
        }

        @Test
        @DisplayName("기존 프로필 이미지를 정리한 뒤 새 이미지로 교체한다")
        void update_profile_image_removes_previous_image_before_replacing() {
            User user = user(USER_ID);
            user.setProfileImageUrl("https://cdn.example.com/old-profile.png");

            MockMultipartFile file = new MockMultipartFile(
                    "profile",
                    "profile.png",
                    "image/png",
                    "hello".getBytes()
            );

            UserRes expected = userRes(
                    USER_ID,
                    "honggildong@example.com",
                    "honggildong",
                    "https://cdn.example.com/new-profile.png",
                    true
            );

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(storage.upload("profiles", file)).thenReturn("https://cdn.example.com/new-profile.png");
            when(userMapper.toRes(user)).thenReturn(expected);

            UserRes result = userService.updateProfileImage(USER_ID, file);

            assertThat(result).isSameAs(expected);
            assertThat(user.getProfileImageUrl()).isEqualTo("https://cdn.example.com/new-profile.png");

            verify(storage).delete("https://cdn.example.com/old-profile.png");
            verify(storage).upload("profiles", file);
        }

        @Test
        @DisplayName("기존 이미지 정리에 실패해도 새 이미지로 교체한다")
        void update_profile_image_replaces_image_even_if_cleanup_fails() {
            User user = user(USER_ID);
            user.setProfileImageUrl("https://cdn.example.com/old-profile.png");

            MockMultipartFile file = new MockMultipartFile(
                    "profile",
                    "profile.png",
                    "image/png",
                    "hello".getBytes()
            );

            UserRes expected = userRes(
                    USER_ID,
                    "honggildong@example.com",
                    "honggildong",
                    "https://cdn.example.com/new-profile.png",
                    true
            );

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            doThrow(new RuntimeException("delete failed"))
                    .when(storage).delete("https://cdn.example.com/old-profile.png");
            when(storage.upload("profiles", file)).thenReturn("https://cdn.example.com/new-profile.png");
            when(userMapper.toRes(user)).thenReturn(expected);

            UserRes result = userService.updateProfileImage(USER_ID, file);

            assertThat(result).isSameAs(expected);
            assertThat(user.getProfileImageUrl()).isEqualTo("https://cdn.example.com/new-profile.png");

            verify(storage).delete("https://cdn.example.com/old-profile.png");
            verify(storage).upload("profiles", file);
        }

        @Test
        @DisplayName("업로드 파일이 비어 있으면 예외가 발생한다")
        void update_profile_image_throw_when_file_is_empty() {
            MockMultipartFile emptyFile = new MockMultipartFile("profile", new byte[0]);

            assertThatThrownBy(() -> userService.updateProfileImage(USER_ID, emptyFile))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.FILE_EMPTY));

            verify(userRepository, never()).findById(any());
            verify(storage, never()).upload(any(), any(MultipartFile.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자가 프로필 이미지를 변경하면 예외가 발생한다")
        void update_profile_image_throw_when_user_does_not_exist() {
            MockMultipartFile file = new MockMultipartFile(
                    "profile",
                    "profile.png",
                    "image/png",
                    "hello".getBytes()
            );

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateProfileImage(USER_ID, file))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));

            verify(storage, never()).delete(any());
            verify(storage, never()).upload(any(), any(MultipartFile.class));
        }
    }

    @Nested
    @DisplayName("deleteProfileImage")
    class DeleteProfileImageTest {
        @Test
        @DisplayName("프로필 이미지를 삭제한다")
        void delete_profile_image_clears_profile_image() {
            User user = user(USER_ID);
            user.setProfileImageUrl("https://cdn.example.com/profile.png");

            UserRes expected = userRes(
                    USER_ID,
                    "honggildong@example.com",
                    "honggildong",
                    null,
                    false
            );

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userMapper.toRes(user)).thenReturn(expected);

            UserRes result = userService.deleteProfileImage(USER_ID);

            assertThat(result).isSameAs(expected);
            assertThat(user.getProfileImageUrl()).isNull();
            verify(storage).delete("https://cdn.example.com/profile.png");
        }

        @Test
        @DisplayName("이미지 정리에 실패해도 프로필 이미지는 제거한다")
        void delete_profile_image_clears_profile_image_even_when_cleanup_fails() {
            User user = user(USER_ID);
            user.setProfileImageUrl("https://cdn.example.com/profile.png");

            UserRes expected = userRes(
                    USER_ID,
                    "honggildong@example.com",
                    "honggildong",
                    null,
                    false
            );

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            doThrow(new RuntimeException("delete failed"))
                    .when(storage).delete("https://cdn.example.com/profile.png");
            when(userMapper.toRes(user)).thenReturn(expected);

            UserRes result = userService.deleteProfileImage(USER_ID);

            assertThat(result).isSameAs(expected);
            assertThat(user.getProfileImageUrl()).isNull();
            verify(storage).delete("https://cdn.example.com/profile.png");
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {
        @Test
        @DisplayName("회원 삭제 전에 로그인 토큰을 정리한다")
        void delete_revokes_tokens_before_removing_user() {
            User user = user(USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            userService.delete(USER_ID);

            InOrder inOrder = inOrder(tokenService, userRepository);
            inOrder.verify(tokenService).revokeAllTokens(USER_ID);
            inOrder.verify(userRepository).delete(user);

            verify(storage, never()).delete(any());
        }

        @Test
        @DisplayName("프로필 이미지가 있으면 함께 정리한 뒤 회원을 삭제한다")
        void delete_removes_profile_image_when_present() {
            User user = user(USER_ID);
            user.setProfileImageUrl("https://cdn.example.com/profile.png");

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            userService.delete(USER_ID);

            verify(tokenService).revokeAllTokens(USER_ID);
            verify(storage).delete("https://cdn.example.com/profile.png");
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("프로필 이미지 정리에 실패해도 회원 삭제는 계속 진행한다")
        void delete_removes_user_even_when_profile_image_cleanup_fails() {
            User user = user(USER_ID);
            user.setProfileImageUrl("https://cdn.example.com/profile.png");

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            doThrow(new RuntimeException("delete failed"))
                    .when(storage).delete("https://cdn.example.com/profile.png");

            userService.delete(USER_ID);

            verify(tokenService).revokeAllTokens(USER_ID);
            verify(storage).delete("https://cdn.example.com/profile.png");
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 삭제 시 예외가 발생한다")
        void delete_throw_when_user_does_not_exist() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.delete(USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));

            verify(tokenService, never()).revokeAllTokens(any());
            verify(storage, never()).delete(any());
            verify(userRepository, never()).delete(any());
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

    private UserRes userRes(Long id, String email, String name, String profileImageUrl, boolean hasCustomProfileImage) {
        return new UserRes(
                id,
                email,
                name,
                profileImageUrl,
                hasCustomProfileImage,
                User.Role.ROLE_USER,
                AuthProvider.GOOGLE,
                FIXED_INSTANT,
                FIXED_INSTANT
        );
    }
}
