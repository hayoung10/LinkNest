package com.linknest.backend.userpreferences;

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
    public ResponseEntity<UserPreferencesRes> get(@AuthenticationPrincipal(expression = "id") Long userId) {
        UserPreferencesRes res = service.get(userId);
        return ResponseEntity.ok(res);
    }

    @PatchMapping
    public ResponseEntity<UserPreferencesRes> update(@AuthenticationPrincipal(expression = "id") Long userId,
                                                     @RequestBody UserPreferencesUpdateReq req) {
        UserPreferencesRes res = service.update(userId, req);
        return ResponseEntity.ok(res);
    }
}
