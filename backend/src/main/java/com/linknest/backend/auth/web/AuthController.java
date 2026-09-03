package com.linknest.backend.auth.web;

import com.linknest.backend.common.response.ApiResponse;
import com.linknest.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequestMapping(API_PREFIX + "/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final AuthService authService;

    @PostMapping("/test-login")
    public ResponseEntity<ApiResponse<Void>> testLogin() {
        ResponseCookie rtCookie = authService.testLogin();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, rtCookie.toString())
                .body(ApiResponse.ok("테스트 계정 로그인 성공", null));
    }
}
