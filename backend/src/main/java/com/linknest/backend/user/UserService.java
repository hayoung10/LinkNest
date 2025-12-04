package com.linknest.backend.user;

import com.linknest.backend.auth.token.TokenService;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.storage.Storage;
import com.linknest.backend.user.dto.UserRes;
import com.linknest.backend.user.dto.UserUpdateReq;
import com.linknest.backend.userpreferences.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final Storage storage;
    private final UserPreferencesRepository userPreferencesRepository;
    private final TokenService tokenService;

    public UserRes get(Long id) {
        return mapper.toRes(findVerifiedUser(id));
    }

    @Transactional
    public UserRes update(Long userId, UserUpdateReq req) {
        User user = findVerifiedUser(userId);
        mapper.updateFromDto(req, user);

        return mapper.toRes(user);
    }

    @Transactional
    public UserRes updateProfileImage(Long userId, MultipartFile profileImage) {
        if(profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("업로드할 프로필 이미지가 비어있습니다.");
        }

        User user = findVerifiedUser(userId);

        // 기존 프로필 이미지 삭제
        String oldUrl = user.getProfileImageUrl();
        if(oldUrl != null && !oldUrl.isBlank()) {
            storage.delete(oldUrl);
        }

        // 새 이미지 업로드
        String newUrl = storage.upload("profiles", profileImage);
        user.setProfileImageUrl(newUrl);

        return mapper.toRes(user);
    }

    @Transactional
    public UserRes deleteProfileImage(Long userId) {
        User user = findVerifiedUser(userId);

        String oldUrl = user.getProfileImageUrl();
        if(oldUrl != null && !oldUrl.isBlank()) {
            try {
                storage.delete(oldUrl);
            } catch (Exception e) {
                log.warn("User profile image delete: failed to delete profile image. userId={}, url={}, reason={}",
                        userId, oldUrl, e.getMessage(), e);
            }
        }
        user.setProfileImageUrl(null);

        return mapper.toRes(user);
    }

    @Transactional
    public void delete(Long userId) {
        User user = findVerifiedUser(userId);

        // 모든 RT 삭제
        tokenService.revokeAllTokens(userId);

        // UserPreferences 삭제
        userPreferencesRepository.deleteByUserId(user.getId());

        // 프로필 이미지 삭제
        String profileImageUrl = user.getProfileImageUrl();
        if(profileImageUrl != null && !profileImageUrl.isBlank()) {
            try {
                storage.delete(profileImageUrl);
            } catch (Exception e) {
                log.warn("User delete: failed to delete profile image. userId={}, url={}, reason={}",
                        userId, profileImageUrl, e.getMessage(), e);
            }
        }

        userRepository.delete(user);
    }

    private User findVerifiedUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
