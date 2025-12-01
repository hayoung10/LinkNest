package com.linknest.backend.user;

import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.storage.Storage;
import com.linknest.backend.user.dto.UserRes;
import com.linknest.backend.user.dto.UserUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final Storage storage;

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
            storage.delete(oldUrl);
        }
        user.setProfileImageUrl(null);

        return mapper.toRes(user);
    }

    @Transactional
    public void delete(Long userId) {
        User user = findVerifiedUser(userId);
        repository.delete(user);
    }

    private User findVerifiedUser(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
