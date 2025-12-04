package com.linknest.backend.auth.token;

import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.config.props.JwtProperties;
import com.linknest.backend.security.jwt.JwtTokenizer;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import io.jsonwebtoken.lang.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenizer jwtTokenizer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    // ---------- 발급 ----------
    public Map<String, String> issueTokens(Long userId, String username, List<String> roles) { // AT/RT 발급
        String jti = UUID.randomUUID().toString();
        String familyId = UUID.randomUUID().toString();

        String accessToken = jwtTokenizer.createAccessToken(userId, username, roles);
        String refreshToken = jwtTokenizer.createRefreshToken(userId, familyId, jti);

        Duration ttl = Duration.ofDays(jwtProperties.getRefreshExpDays());
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .id(jti)
                .userId(userId)
                .token(refreshToken)
                .expiration(Instant.now().plus(ttl))
                .build();
        refreshTokenRepository.save(entity, ttl);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    // ---------- 재발급 ----------
    public Map<String, String> rotateTokens(String refreshToken) {
        JwtTokenizer.RtClaims rtClaims = jwtTokenizer.parseAndValidateRefresh(refreshToken);

        // RT 확인
        RefreshTokenEntity findEntity = refreshTokenRepository.find(rtClaims.jti())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        if(!Objects.equals(findEntity.getToken(), refreshToken)) {
            throw new BusinessException(ErrorCode.MISMATCH_REFRESH_TOKEN);
        }

        // 사용자 확인
        User user = userRepository.findById(rtClaims.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String username = user.getName();
        List<String> roles = List.of(user.getRole().name());

        // 기존 RT 폐기
        refreshTokenRepository.delete(rtClaims.jti());

        // 새 AT/RT 발급
        String newJti = UUID.randomUUID().toString();
        String newAccessToken = jwtTokenizer.createAccessToken(rtClaims.userId(), username, roles);
        String newRefreshToken = jwtTokenizer.createRefreshToken(rtClaims.userId(), rtClaims.familyId(), newJti);

        Duration ttl = Duration.ofDays(jwtProperties.getRefreshExpDays());
        RefreshTokenEntity newEntity = RefreshTokenEntity.builder()
                .id(newJti)
                .userId(rtClaims.userId())
                .token(newRefreshToken)
                .expiration(Instant.now().plus(ttl))
                .build();
        refreshTokenRepository.save(newEntity, ttl);

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    // ---------- 폐기 ----------
    public void revokeToken(String refreshToken) {
        JwtTokenizer.RtClaims rtClaims = jwtTokenizer.parseAndValidateRefresh(refreshToken);
        refreshTokenRepository.delete(rtClaims.jti());
    }

    public void revokeAllTokens(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }
}
