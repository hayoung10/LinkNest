package com.linknest.backend.security.jwt;

import com.linknest.backend.config.props.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.sql.Date;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenizer {
    private final JwtProperties properties;

    private SecretKey key() {
        String base64Secret = properties.getSecret();
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(key())
                .requireIssuer(properties.getIssuer())
                .setAllowedClockSkewSeconds(properties.getClockSkewSeconds())
                .build()
                .parseClaimsJws(token);

        return jws.getBody();
    }

    public String createAccessToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getAccessExpMinutes() * 60L);

        return Jwts.builder()
                .setIssuer(properties.getIssuer())
                .setAudience(properties.getAudience())
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key())
                .compact();
    }

    public String createRefreshToken(Long userId, String familyId, String jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getRefreshExpDays() * 24L * 3600L);

        return Jwts.builder()
                .setIssuer(properties.getIssuer())
                .setSubject(String.valueOf(userId))
                .setId(jti)
                .claim("fid", familyId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key())
                .compact();
    }

    // AT 검증 & 필요한 클레임 추출
    public AtClaims parseAndValidateAccess(String token) {
        Claims claims = parseClaims(token);

        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles", List.class);
        if(roles == null) {
            roles = java.util.Collections.emptyList();
        }

        return new AtClaims(userId, username, roles);
    }

    // RT 검증 & 필요한 클레임 추출
    public RtClaims parseAndValidateRefresh(String token) {
        Claims claims = parseClaims(token);

        Long userId = Long.valueOf(claims.getSubject());
        String jti = claims.getId();
        String familyId = claims.get("fid", String.class);

        return new RtClaims(userId, jti, familyId);
    }

    // DTO
    public record AtClaims(Long userId, String username, List<String> roles) {}
    public record RtClaims(Long userId, String jti, String familyId) {}
}
