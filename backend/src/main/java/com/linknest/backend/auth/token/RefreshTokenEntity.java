package com.linknest.backend.auth.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity implements Serializable {
    private String id;
    private Long userId;
    private String token;
    private Instant expiration;
}
