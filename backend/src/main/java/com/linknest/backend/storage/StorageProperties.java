package com.linknest.backend.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private StorageProvider provider = StorageProvider.LOCAL;

    private Local local = new Local();
    private S3 s3 = new S3();

    public enum StorageProvider {
        LOCAL,
        S3
    }

    @Getter
    @Setter
    public static class Local {
        private String basePath;
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucket;
        private String baseUrl;
        private String region;
    }
}
