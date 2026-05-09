package com.linknest.backend.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.upload-dir")
public record UploadProperties(
    String userProfileDir,
    String providerProfileDir,
    String bookmarkCoverDir
) {}
