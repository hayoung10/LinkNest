package com.linknest.backend.user;

import com.linknest.backend.storage.Storage;
import com.linknest.backend.user.domain.AuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderProfileImageService {
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;
    private static final int MAX_CONTENT_LENGTH = 5 * 1024 * 1024; // 5MB

    private final Storage storage;

    public String syncProviderProfileImage(
            AuthProvider provider,
            String providerId,
            String sourceImageUrl,
            String currentStoredUrl
    ) {
        if(sourceImageUrl == null || sourceImageUrl.isBlank()) {
            return currentStoredUrl;
        }

        if(currentStoredUrl != null && !currentStoredUrl.isBlank()) {
            log.debug("Provider profile image sync: reuse existing. provider={}, providerId={}",
                    provider, providerId);
            return currentStoredUrl;
        }

        HttpURLConnection connection = null;

        try {
            URL url = new URL(sourceImageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);

            int status = connection.getResponseCode();
            if(status != HttpURLConnection.HTTP_OK) {
                log.warn("Provider profile image sync: non-200 response. provider={}, providerId={}, status={}, url={}",
                        provider, providerId, status, sourceImageUrl);
                return currentStoredUrl;
            }

            String contentType = connection.getContentType();
            if(!isSupportedImageContentType(contentType)) {
                log.warn("Provider profile image sync: unsupported content type. provider={}, providerId={}, contentType={}, url={}",
                        provider, providerId, contentType, sourceImageUrl);
                return currentStoredUrl;
            }

            int contentLength = connection.getContentLength();
            if(contentLength > MAX_CONTENT_LENGTH) {
                log.warn("Provider profile image sync: file too large. provider={}, providerId={}, contentLength={}, url={}",
                        provider, providerId, contentLength, sourceImageUrl);
                return currentStoredUrl;
            }

            String directory = buildDirectory(provider);

            String newStoredUrl;
            try (InputStream inputStream = connection.getInputStream()) {
                newStoredUrl = storage.upload(directory, inputStream, contentType);
            }

            deletePreviousProviderImage(currentStoredUrl, newStoredUrl, provider, providerId);

            log.debug("Provider profile image sync: success. provider={}, providerId={}", provider, providerId);
            return newStoredUrl;
        } catch (Exception e) {
            log.warn("Provider profile image sync: failed. provider={}, providerId={}, sourceUrl={}",
                    provider, providerId, sourceImageUrl, e);
            return currentStoredUrl;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isSupportedImageContentType(String contentType) {
        if(contentType == null || contentType.isBlank()) {
            return false;
        }

        return switch(contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp" -> true;
            default -> false;
        };
    }

    private String buildDirectory(AuthProvider provider) {
        return "uploads/profiles/providers/" + provider.name().toLowerCase(Locale.ROOT);
    }

    private void deletePreviousProviderImage(
            String currentStoredUrl,
            String newStoredUrl,
            AuthProvider provider,
            String providerId
    ) {
        if(currentStoredUrl == null || currentStoredUrl.isBlank()) {
            return;
        }

        if(currentStoredUrl.equals(newStoredUrl)) {
            return;
        }

        try {
            storage.delete(currentStoredUrl);
        } catch (Exception e) {
            log.warn("Provider profile image sync: failed to delete previous image. provider={}, providerId={}, url={}",
                    provider, providerId, currentStoredUrl, e);
        }
    }
}
