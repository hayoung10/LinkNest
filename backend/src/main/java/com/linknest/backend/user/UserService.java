package com.linknest.backend.user;

import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.user.dto.UserRes;
import com.linknest.backend.user.dto.UserUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserRes get(Long id) {
        return mapper.toRes(findVerifiedUser(id));
    }

    @Transactional
    public UserRes update(Long id, UserUpdateReq req) {
        User user = findVerifiedUser(id);
        mapper.updateFromDto(req, user);

        return mapper.toRes(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = findVerifiedUser(id);
        repository.delete(user);
    }

    private User findVerifiedUser(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
