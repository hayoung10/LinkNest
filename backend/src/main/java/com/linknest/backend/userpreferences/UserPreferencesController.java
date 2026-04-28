package com.linknest.backend.userpreferences;

import com.linknest.backend.common.response.ApiResponse;
import com.linknest.backend.userpreferences.dto.UserPreferencesRes;
import com.linknest.backend.userpreferences.dto.UserPreferencesUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/users/me/preferences")
public class UserPreferencesController {
    private final UserPreferencesService service;

    @GetMapping
    public ResponseEntity<ApiResponse<UserPreferencesRes>> get(@AuthenticationPrincipal(expression = "id") Long userId) {
        UserPreferencesRes data = service.get(userId);
        return ResponseEntity.ok(ApiResponse.ok("환경설정 조회", data));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserPreferencesRes>> update(@AuthenticationPrincipal(expression = "id") Long userId,
                                                     @RequestBody UserPreferencesUpdateReq req) {
        UserPreferencesRes data = service.update(userId, req);
        return ResponseEntity.ok(ApiResponse.ok("환경설정 수정", data));
    }
}
