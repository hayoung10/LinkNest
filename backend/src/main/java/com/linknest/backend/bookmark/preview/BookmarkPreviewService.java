package com.linknest.backend.bookmark.preview;

import java.util.Optional;

public interface BookmarkPreviewService {
    Optional<String> extractAutoImageUrl(String url);
}
