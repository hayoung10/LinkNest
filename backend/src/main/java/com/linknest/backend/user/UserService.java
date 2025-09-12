package com.linknest.backend.user;

import com.linknest.backend.user.dto.UserCreateReq;
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

    @Transactional
    public UserRes create(UserCreateReq req) {
        if(repository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("User exists: " + req.email());
        }

        User user = mapper.toEntity(req);
        user.setRole(User.Role.ROLE_USER);

        return mapper.toRes(repository.save(user));
    }

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
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }
}
