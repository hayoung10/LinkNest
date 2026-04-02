package com.linknest.backend.trash.purge;

import com.linknest.backend.bookmark.BookmarkRepository;
import com.linknest.backend.collection.CollectionRepository;
import com.linknest.backend.tag.TagRepository;
import com.linknest.backend.tag.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrashPurgeServiceTest {
    private static final Instant FIXED_INSTANT = Instant.parse("2026-04-02T00:00:00Z");

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagService tagService;

    private Clock clock;
    private TrashPurgeService trashPurgeService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        trashPurgeService = new TrashPurgeService(
                clock,
                collectionRepository,
                bookmarkRepository,
                tagRepository,
                tagService
        );
    }

    @Nested
    @DisplayName("purgeExpired")
    class PurgeExpiredTest {
        @Test
        @DisplayName("만료된 휴지통 데이터를 모두 정리한다")
        void purge_removes_all_expired_trash_items() {
            Instant cutoff = FIXED_INSTANT.minus(30, ChronoUnit.DAYS);

            when(collectionRepository.findExpiredDeletedRootIds(cutoff, 100))
                    .thenReturn(List.of(10L))
                    .thenReturn(List.of());
            when(collectionRepository.findSubtreeIdsByRootIds(List.of(10L)))
                    .thenReturn(List.of(10L, 11L));
            when(bookmarkRepository.findTagIdsByDeletedBookmarksInCollectionIds(List.of(10L, 11L)))
                    .thenReturn(Set.of(1000L));
            when(collectionRepository.deleteDeletedByIdIn(List.of(10L))).thenReturn(1);

            when(bookmarkRepository.findExpiredDeletedBookmarkIds(cutoff, 100))
                    .thenReturn(List.of(20L, 21L))
                    .thenReturn(List.of());
            when(bookmarkRepository.findTagIdsByDeletedBookmarkIds(List.of(20L, 21L)))
                    .thenReturn(Set.of(2000L));
            when(bookmarkRepository.deleteDeletedByIdIn(List.of(20L, 21L))).thenReturn(2);

            when(tagRepository.findExpiredDeletedTagIds(cutoff, 100))
                    .thenReturn(List.of(30L))
                    .thenReturn(List.of());
            when(tagRepository.deleteDeletedByIdIn(List.of(30L))).thenReturn(1);

            TrashPurgeResult result = trashPurgeService.purgeExpired(100, 30);

            assertThat(result.deletedCollections()).isEqualTo(1);
            assertThat(result.deletedBookmarks()).isEqualTo(2);
            assertThat(result.deletedTags()).isEqualTo(1);

            assertThat(result.scannedCollections()).isEqualTo(1);
            assertThat(result.scannedBookmarks()).isEqualTo(2);
            assertThat(result.scannedTags()).isEqualTo(1);

            assertThat(result.cutoff()).isEqualTo(cutoff);
        }

        @Test
        @DisplayName("정리된 항목에서 연관된 태그 상태를 함께 반영한다")
        void purge_updates_related_tags_when_items_are_removed() {
            Instant cutoff = FIXED_INSTANT.minus(7, ChronoUnit.DAYS);

            when(collectionRepository.findExpiredDeletedRootIds(cutoff, 50))
                    .thenReturn(List.of(10L))
                    .thenReturn(List.of());
            when(collectionRepository.findSubtreeIdsByRootIds(List.of(10L)))
                    .thenReturn(List.of(10L, 11L));
            when(bookmarkRepository.findTagIdsByDeletedBookmarksInCollectionIds(List.of(10L, 11L)))
                    .thenReturn(Set.of(100L, 200L));
            when(collectionRepository.deleteDeletedByIdIn(List.of(10L))).thenReturn(1);

            when(bookmarkRepository.findExpiredDeletedBookmarkIds(cutoff, 50))
                    .thenReturn(List.of(20L))
                    .thenReturn(List.of());
            when(bookmarkRepository.findTagIdsByDeletedBookmarkIds(List.of(20L)))
                    .thenReturn(Set.of(200L, 300L));
            when(bookmarkRepository.deleteDeletedByIdIn(List.of(20L))).thenReturn(1);

            when(tagRepository.findExpiredDeletedTagIds(cutoff, 50))
                    .thenReturn(List.of())
                    .thenReturn(List.of());

            trashPurgeService.purgeExpired(50, 7);

            ArgumentCaptor<Set<Long>> tagIdsCaptor = ArgumentCaptor.forClass(Set.class);
            verify(tagService).onTagsDetached(tagIdsCaptor.capture(), eq(FIXED_INSTANT));

            assertThat(tagIdsCaptor.getValue()).containsExactlyInAnyOrder(100L, 200L, 300L);
        }

        @Test
        @DisplayName("연관된 태그가 없으면 태그 상태를 변경하지 않는다")
        void purge_does_not_update_tags_when_no_related_tags_exist() {
            Instant cutoff = FIXED_INSTANT.minus(14, ChronoUnit.DAYS);

            when(collectionRepository.findExpiredDeletedRootIds(cutoff, 30))
                    .thenReturn(List.of(10L))
                    .thenReturn(List.of());
            when(collectionRepository.findSubtreeIdsByRootIds(List.of(10L)))
                    .thenReturn(List.of(10L));
            when(bookmarkRepository.findTagIdsByDeletedBookmarksInCollectionIds(List.of(10L)))
                    .thenReturn(Set.of());
            when(collectionRepository.deleteDeletedByIdIn(List.of(10L))).thenReturn(1);

            when(bookmarkRepository.findExpiredDeletedBookmarkIds(cutoff, 30))
                    .thenReturn(List.of(20L))
                    .thenReturn(List.of());
            when(bookmarkRepository.findTagIdsByDeletedBookmarkIds(List.of(20L)))
                    .thenReturn(Set.of());
            when(bookmarkRepository.deleteDeletedByIdIn(List.of(20L))).thenReturn(1);

            when(tagRepository.findExpiredDeletedTagIds(cutoff, 30))
                    .thenReturn(List.of(30L))
                    .thenReturn(List.of());
            when(tagRepository.deleteDeletedByIdIn(List.of(30L))).thenReturn(1);

            trashPurgeService.purgeExpired(30, 14);

            verify(tagService, never()).onTagsDetached(any(), any());
        }

        @Test
        @DisplayName("정리할 데이터가 없으면 즉시 종료한다")
        void purge_stops_when_no_more_items_to_clean() {
            Instant cutoff = FIXED_INSTANT.minus(10, ChronoUnit.DAYS);

            when(collectionRepository.findExpiredDeletedRootIds(cutoff, 20))
                    .thenReturn(List.of());
            when(bookmarkRepository.findExpiredDeletedBookmarkIds(cutoff, 20))
                    .thenReturn(List.of());
            when(tagRepository.findExpiredDeletedTagIds(cutoff, 20))
                    .thenReturn(List.of());

            TrashPurgeResult result = trashPurgeService.purgeExpired(20, 10);

            assertThat(result.deletedCollections()).isZero();
            assertThat(result.deletedBookmarks()).isZero();
            assertThat(result.deletedTags()).isZero();

            assertThat(result.scannedCollections()).isZero();
            assertThat(result.scannedBookmarks()).isZero();
            assertThat(result.scannedTags()).isZero();

            verify(collectionRepository, times(1)).findExpiredDeletedRootIds(cutoff, 20);
            verify(bookmarkRepository, times(1)).findExpiredDeletedBookmarkIds(cutoff, 20);
            verify(tagRepository, times(1)).findExpiredDeletedTagIds(cutoff, 20);

            verify(tagService, never()).onTagsDetached(any(), any());
        }

        @Test
        @DisplayName("데이터가 남아 있으면 여러 번 나눠서 정리한다")
        void purge_processes_items_over_multiple_rounds() {
            Instant cutoff = FIXED_INSTANT.minus(5, ChronoUnit.DAYS);

            when(collectionRepository.findExpiredDeletedRootIds(cutoff, 10))
                    .thenReturn(List.of(10L, 11L))
                    .thenReturn(List.of(12L))
                    .thenReturn(List.of());
            when(collectionRepository.findSubtreeIdsByRootIds(List.of(10L, 11L)))
                    .thenReturn(List.of(10L, 11L, 110L));
            when(collectionRepository.findSubtreeIdsByRootIds(List.of(12L)))
                    .thenReturn(List.of(12L));
            when(bookmarkRepository.findTagIdsByDeletedBookmarksInCollectionIds(List.of(10L, 11L, 110L)))
                    .thenReturn(Set.of(100L));
            when(bookmarkRepository.findTagIdsByDeletedBookmarksInCollectionIds(List.of(12L)))
                    .thenReturn(Set.of());
            when(collectionRepository.deleteDeletedByIdIn(List.of(10L, 11L))).thenReturn(2);
            when(collectionRepository.deleteDeletedByIdIn(List.of(12L))).thenReturn(1);

            when(bookmarkRepository.findExpiredDeletedBookmarkIds(cutoff, 10))
                    .thenReturn(List.of(20L))
                    .thenReturn(List.of(21L, 22L))
                    .thenReturn(List.of());
            when(bookmarkRepository.findTagIdsByDeletedBookmarkIds(List.of(20L)))
                    .thenReturn(Set.of(200L));
            when(bookmarkRepository.findTagIdsByDeletedBookmarkIds(List.of(21L, 22L)))
                    .thenReturn(Set.of(300L));
            when(bookmarkRepository.deleteDeletedByIdIn(List.of(20L))).thenReturn(1);
            when(bookmarkRepository.deleteDeletedByIdIn(List.of(21L, 22L))).thenReturn(2);

            when(tagRepository.findExpiredDeletedTagIds(cutoff, 10))
                    .thenReturn(List.of(30L))
                    .thenReturn(List.of(31L))
                    .thenReturn(List.of());
            when(tagRepository.deleteDeletedByIdIn(List.of(30L))).thenReturn(1);
            when(tagRepository.deleteDeletedByIdIn(List.of(31L))).thenReturn(1);

            TrashPurgeResult result = trashPurgeService.purgeExpired(10, 5);

            assertThat(result.deletedCollections()).isEqualTo(3);
            assertThat(result.deletedBookmarks()).isEqualTo(3);
            assertThat(result.deletedTags()).isEqualTo(2);

            assertThat(result.scannedCollections()).isEqualTo(3);
            assertThat(result.scannedBookmarks()).isEqualTo(3);
            assertThat(result.scannedTags()).isEqualTo(2);

            verify(tagService, times(2)).onTagsDetached(anySet(), eq(FIXED_INSTANT));
        }

        @Test
        @DisplayName("정리 결과를 요약 정보로 반환한다")
        void purge_returns_summary_of_cleanup_result() {
            Instant cutoff = FIXED_INSTANT.minus(30, ChronoUnit.DAYS);

            when(collectionRepository.findExpiredDeletedRootIds(cutoff, 5))
                    .thenReturn(List.of(10L))
                    .thenReturn(List.of());
            when(collectionRepository.findSubtreeIdsByRootIds(List.of(10L)))
                    .thenReturn(List.of(10L));
            when(bookmarkRepository.findTagIdsByDeletedBookmarksInCollectionIds(List.of(10L)))
                    .thenReturn(Set.of());
            when(collectionRepository.deleteDeletedByIdIn(List.of(10L))).thenReturn(1);

            when(bookmarkRepository.findExpiredDeletedBookmarkIds(cutoff, 5))
                    .thenReturn(List.of(20L, 21L))
                    .thenReturn(List.of());
            when(bookmarkRepository.findTagIdsByDeletedBookmarkIds(List.of(20L, 21L)))
                    .thenReturn(Set.of());
            when(bookmarkRepository.deleteDeletedByIdIn(List.of(20L, 21L))).thenReturn(2);

            when(tagRepository.findExpiredDeletedTagIds(cutoff, 5))
                    .thenReturn(List.of())
                    .thenReturn(List.of());

            TrashPurgeResult result = trashPurgeService.purgeExpired(5, 30);

            assertThat(result).isEqualTo(new TrashPurgeResult(
                    1,
                    2,
                    0,
                    1,
                    2,
                    0,
                    cutoff
            ));
        }
    }
}
