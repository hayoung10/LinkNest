package com.linknest.backend.config.props;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    @NotBlank
    private String issuer;

    @NotBlank
    private String audience;

    @NotBlank
    private String secret;

    @Min(1)
    private int accessExpMinutes;

    @Min(1)
    private int refreshExpDays;

    @NotBlank
    private String header;

    @NotBlank
    private String prefix;

    @Min(0)
    private long clockSkewSeconds;
}
