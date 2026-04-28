package com.linknest.backend.bookmark.preview;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class JsoupBookmarkPreviewService implements BookmarkPreviewService {
    private static final int TIMEOUT_MS = (int) Duration.ofSeconds(3).toMillis();

    @Override
    public AutoImageResult extractAutoImage(String url) {
        try {
            // HTML 파싱
            Document doc = Jsoup.connect(url)
                    .userAgent("LinkNestBot/1.0")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            // Open Graph 대표 이미지 확인
            Optional<String> ogImage = selectMetaAbsUrl(doc, "property", "og:image");
            if(ogImage.isPresent()) return AutoImageResult.success(ogImage.get());

            // Twitter Card 이미지 확인
            Optional<String> twImage = selectMetaAbsUrl(doc, "name", "twitter:image");
            if(twImage.isPresent()) return AutoImageResult.success(twImage.get());

            // 없으면 favicon 반환
            Optional<String> favicon = extractFavicon(url, doc);
            if(favicon.isPresent()) return AutoImageResult.success(favicon.get());

            return AutoImageResult.failed();
        } catch (Exception e) {
            log.debug("BookmarkPreview: failed to extract auto image. url={}", url, e);
            return AutoImageResult.failed();
        }
    }

    // meta 태그 content 값을 절대 URL로 반환
    private Optional<String> selectMetaAbsUrl(Document doc, String attrKey, String attrValue) {
        Element element = doc.selectFirst("meta[" + attrKey + "=" + attrValue + "]");
        if(element == null) return Optional.empty();

        String content = element.attr("content");
        if(content == null || content.isBlank()) return Optional.empty();

        // 절대경로 반환
        String absUrl = resolveAbsUrl(doc.baseUri(), content);
        return (absUrl == null || absUrl.isBlank()) ? Optional.empty() : Optional.of(absUrl);
    }

    // favicon 추출
    private Optional<String> extractFavicon(String pageUrl, Document doc) {
        Element icon = doc.selectFirst("link[rel~=(?i)icon|apple-touch-icon]");
        if(icon != null) {
            String href = icon.attr("href");
            String abs = resolveAbsUrl(doc.baseUri(), href);
            if(abs != null && !abs.isBlank()) return Optional.of(abs);
        }

        // 기본 경로 반환 (https://{host}/favicon.ico)
        try {
            URI uri = URI.create(pageUrl);
            if(uri.getScheme() == null || uri.getHost() == null) return Optional.empty();
            String base = uri.getScheme() + "://" + uri.getHost();
            return Optional.of(base + "/favicon.ico");
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    // 절대 URL 반환
    private String resolveAbsUrl(String baseUri, String urlOrPath) {
        if(urlOrPath == null || urlOrPath.isBlank()) return null;

        try {
            URI uri = URI.create(urlOrPath);
            if(uri.isAbsolute()) return urlOrPath;
        } catch (Exception ignored) {
        }

        // urlOrPath가 상대경로인 경우
        try {
            return URI.create(baseUri).resolve(urlOrPath).toString();
        } catch (Exception e) {
            return urlOrPath;
        }
    }
}
