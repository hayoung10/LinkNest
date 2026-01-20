package com.linknest.backend.user;

import com.linknest.backend.common.response.ApiResponse;
import com.linknest.backend.common.utils.CookieUtils;
import com.linknest.backend.user.dto.UserRes;
import com.linknest.backend.user.dto.UserUpdateReq;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
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
    public ResponseEntity<ApiResponse<UserRes>> me(@AuthenticationPrincipal(expression = "id") Long userId) {
        UserRes data = service.get(userId);
        return ResponseEntity.ok(ApiResponse.ok("내 정보 조회", data));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserRes>> update(@AuthenticationPrincipal(expression = "id") Long userId,
                                          @RequestBody @Valid UserUpdateReq req) {
        UserRes data = service.update(userId, req);
        return ResponseEntity.ok(ApiResponse.ok("내 정보 수정", data));
    }

    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserRes>> updateProfileImage(@AuthenticationPrincipal(expression = "id") Long userId,
                                                      @RequestPart("file") MultipartFile profileImage) {
        UserRes data = service.updateProfileImage(userId, profileImage);
        return ResponseEntity.ok(ApiResponse.ok("프로필 이미지 수정", data));
    }

    @DeleteMapping(value = "/profile-image")
    public ResponseEntity<ApiResponse<UserRes>> deleteProfileImage(@AuthenticationPrincipal(expression = "id") Long userId) {
        UserRes data = service.deleteProfileImage(userId);
        return ResponseEntity.ok(ApiResponse.ok("프로필 이미지 삭제", data));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal(expression = "id") Long userId,
                                       HttpServletResponse response) {
        service.delete(userId);

        // RT 쿠키 삭제
        ResponseCookie delCookie = CookieUtils.deleteCookie("refresh_token");
        response.addHeader(HttpHeaders.SET_COOKIE, delCookie.toString());

        return ResponseEntity.ok(ApiResponse.ok("회원 탈퇴 완료"));
    }
}
