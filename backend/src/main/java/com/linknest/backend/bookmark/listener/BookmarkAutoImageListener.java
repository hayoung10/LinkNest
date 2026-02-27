package com.linknest.backend.bookmark.listener;

import com.linknest.backend.bookmark.BookmarkRepository;
import com.linknest.backend.bookmark.event.BookmarkAutoImageRequestedEvent;
import com.linknest.backend.bookmark.preview.BookmarkPreviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookmarkAutoImageListener {
    private final BookmarkPreviewService previewService;
    private final BookmarkRepository bookmarkRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BookmarkAutoImageRequestedEvent e) {
        log.info("AutoImageListener thread = {}", Thread.currentThread().getName());
        try {
            String auto = previewService.extractAutoImageUrl(e.url()).orElse(null);
            if(auto == null || auto.isBlank()) return;

            // imageMode/url이 바꿨거나 이미 채워져있는지 확인
            int updated = bookmarkRepository.updateAutoImageUrlIfAutoMode(
                    e.userId(), e.bookmarkId(), e.url(), auto);

            if(updated == 0) {
                log.debug("AutoImage skipped. userId={}, bookmarkId={}", e.userId(), e.bookmarkId());
            }
        } catch(Exception ex) {
            log.debug("AutoImage async failed. userId={}, bookmarkId={}, reason={}",
                    e.userId(), e.bookmarkId(), ex.getMessage());
        }
    }
}
