package com.linknest.backend.user;

import com.linknest.backend.user.dto.UserRes;
import com.linknest.backend.user.dto.UserUpdateReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/users/me")
public class UserController {
    private final UserService service;

    @GetMapping
    public ResponseEntity<UserRes> me(@AuthenticationPrincipal(expression = "id") Long userId) {
        UserRes res = service.get(userId);
        return ResponseEntity.ok(res);
    }

    @PatchMapping
    public ResponseEntity<UserRes> update(@AuthenticationPrincipal(expression = "id") Long userId,
                                          @RequestBody @Valid UserUpdateReq req) {
        UserRes res = service.update(userId, req);
        return ResponseEntity.ok(res);
    }

    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserRes> updateProfileImage(@AuthenticationPrincipal(expression = "id") Long userId,
                                                      @RequestPart("file") MultipartFile profileImage) {
        UserRes res = service.updateProfileImage(userId, profileImage);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping(value = "/profile-image")
    public ResponseEntity<UserRes> deleteProfileImage(@AuthenticationPrincipal(expression = "id") Long userId) {
        UserRes res = service.deleteProfileImage(userId);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal(expression = "id") Long userId) {
        service.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
