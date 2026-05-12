package com.linknest.backend.trash;

import com.linknest.backend.bookmark.BookmarkService;
import com.linknest.backend.collection.CollectionService;
import com.linknest.backend.common.dto.SliceResponse;
import com.linknest.backend.tag.Tag;
import com.linknest.backend.tag.TagService;
import com.linknest.backend.trash.domain.TrashType;
import com.linknest.backend.trash.dto.TrashBookmarkRow;
import com.linknest.backend.trash.dto.TrashBulkItem;
import com.linknest.backend.trash.dto.TrashCollectionRow;
import com.linknest.backend.trash.dto.TrashItemRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrashServiceTest {
    private static final Long USER_ID = 1L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-04-01T00:00:00Z");

    @Mock
    private TrashRepository trashRepository;

    @Mock
    private CollectionService collectionService;

    @Mock
    private BookmarkService bookmarkService;

    @Mock
    private TagService tagService;

    @Mock
    private TrashMapper trashMapper;

    private TrashService trashService;

    @BeforeEach
    void setup() {
        trashService = new TrashService(
                trashRepository,
                collectionService,
                bookmarkService,
                tagService,
                trashMapper
        );
    }

    @Nested
    @DisplayName("list")
    class ListTest {
        @Test
        @DisplayName("통합 목록을 삭제된 시간순으로 합쳐서 반환한다")
        void list_fetches_all_trash_items_and_sorts_by_deleted_at() {
            TrashCollectionRow collectionRow = collectionRow(10L, "컬렉션", FIXED_INSTANT.minusSeconds(60));
            TrashBookmarkRow bookmarkRow = bookmarkRow(20L, "북마크", FIXED_INSTANT.minusSeconds(30));
            Tag tag = deletedTag(30L, "태그", FIXED_INSTANT.minusSeconds(10));

            TrashItemRes collectionItemRes = collectionItemRes(10L, "컬렉션", FIXED_INSTANT.minusSeconds((60)));
            TrashItemRes bookmarkItemRes = bookmarkItemRes(20L, "북마크", FIXED_INSTANT.minusSeconds(30));
            TrashItemRes tagItemRes = tagItemRes(30L, "태그", FIXED_INSTANT.minusSeconds(10));

            when(trashRepository.findDeletedCollections(USER_ID, 0, 11)).thenReturn(List.of(collectionRow));
            when(trashRepository.findDeletedBookmarks(USER_ID, 0, 11)).thenReturn(List.of(bookmarkRow));
            when(trashRepository.findDeletedTags(USER_ID, 0, 11)).thenReturn(List.of(tag));

            when(trashMapper.fromCollection(collectionRow)).thenReturn(collectionItemRes);
            when(trashMapper.fromBookmarkRow(bookmarkRow)).thenReturn(bookmarkItemRes);
            when(trashMapper.fromTag(tag)).thenReturn(tagItemRes);

            when(trashRepository.countDeletedChildCollections(USER_ID, List.of(10L)))
                    .thenReturn(Map.of(10L, 1L));
            when(trashRepository.countDeletedBookmarksInCollections(USER_ID, List.of(10L)))
                    .thenReturn(Map.of(10L, 2L));
            when(trashRepository.countTaggedCountByTagIds(USER_ID, List.of(30L)))
                    .thenReturn(Map.of(30L, 3L));

            SliceResponse<TrashItemRes> result = trashService.list(USER_ID, null, 0, 10);

            assertThat(result.items()).hasSize(3);
            assertThat(result.items()).extracting(TrashItemRes::type)
                    .containsExactly(TrashType.TAG, TrashType.BOOKMARK, TrashType.COLLECTION);
            assertThat(result.items()).extracting(TrashItemRes::id)
                    .containsExactly(30L, 20L, 10L);
        }

        @Test
        @DisplayName("통합 목록에서 컬렉션과 태그의 개수를 함께 포함한다")
        void list_enriches_counts_for_collections_and_tags() {
            TrashCollectionRow collectionRow = collectionRow(10L, "컬렉션", FIXED_INSTANT.minusSeconds(60));
            Tag tag = deletedTag(30L, "태그", FIXED_INSTANT.minusSeconds(10));

            TrashItemRes collectionItemRes = collectionItemRes(10L, "컬렉션", FIXED_INSTANT.minusSeconds((60)));
            TrashItemRes tagItemRes = tagItemRes(30L, "태그", FIXED_INSTANT.minusSeconds(10));

            when(trashRepository.findDeletedCollections(USER_ID, 0, 11)).thenReturn(List.of(collectionRow));
            when(trashRepository.findDeletedBookmarks(USER_ID, 0, 11)).thenReturn(List.of());
            when(trashRepository.findDeletedTags(USER_ID, 0, 11)).thenReturn(List.of(tag));

            when(trashMapper.fromCollection(collectionRow)).thenReturn(collectionItemRes);
            when(trashMapper.fromTag(tag)).thenReturn(tagItemRes);

            when(trashRepository.countDeletedChildCollections(USER_ID, List.of(10L)))
                    .thenReturn(Map.of(10L, 4L));
            when(trashRepository.countDeletedBookmarksInCollections(USER_ID, List.of(10L)))
                    .thenReturn(Map.of(10L, 7L));
            when(trashRepository.countTaggedCountByTagIds(USER_ID, List.of(30L)))
                    .thenReturn(Map.of(30L, 5L));

            SliceResponse<TrashItemRes> result = trashService.list(USER_ID, null, 0, 10);

            assertThat(result.items()).hasSize(2);

            TrashItemRes enrichedTag = result.items().get(0);
            TrashItemRes enrichedCollection = result.items().get(1);

            assertThat(enrichedTag.type()).isEqualTo(TrashType.TAG);
            assertThat(enrichedTag.taggedCount()).isEqualTo(5L);

            assertThat(enrichedCollection.type()).isEqualTo(TrashType.COLLECTION);
            assertThat(enrichedCollection.childCount()).isEqualTo(4L);
            assertThat(enrichedCollection.bookmarkCount()).isEqualTo(7L);
        }

        @Test
        @DisplayName("컬렉션 휴지통 목록 조회 시 개수를 포함해서 반환한다")
        void list_returns_deleted_collections_without_counts() {
            TrashCollectionRow row = collectionRow(10L, "컬렉션", FIXED_INSTANT.minusSeconds(60));
            TrashItemRes mapped = collectionItemRes(10L, "컬렉션", FIXED_INSTANT.minusSeconds((60)));

            when(trashRepository.findDeletedCollections(USER_ID, 0, 11)).thenReturn(List.of(row));
            when(trashMapper.fromCollection(row)).thenReturn(mapped);
            when(trashRepository.countDeletedChildCollections(USER_ID, List.of(10L)))
                    .thenReturn(Map.of(10L, 2L));
            when(trashRepository.countDeletedBookmarksInCollections(USER_ID, List.of(10L)))
                    .thenReturn(Map.of(10L, 3L));
            when(trashRepository.countTaggedCountByTagIds(USER_ID, List.of()))
                    .thenReturn(Map.of());

            SliceResponse<TrashItemRes> result = trashService.list(USER_ID, TrashType.COLLECTION, 0, 10);

            assertThat(result.items()).hasSize(1);

            TrashItemRes enrichedCollection = result.items().getFirst();

            assertThat(enrichedCollection.type()).isEqualTo(TrashType.COLLECTION);
            assertThat(enrichedCollection.childCount()).isEqualTo(2L);
            assertThat(enrichedCollection.bookmarkCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("북마크 휴지통 목록 조회 시 북마크 목록만 반환한다")
        void list_returns_deleted_bookmarks_without_extra_counts() {
            TrashBookmarkRow row = bookmarkRow(20L, "북마크", FIXED_INSTANT.minusSeconds(30));
            TrashItemRes mapped = bookmarkItemRes(20L, "북마크", FIXED_INSTANT.minusSeconds((30)));

            when(trashRepository.findDeletedBookmarks(USER_ID, 0, 11)).thenReturn(List.of(row));
            when(trashMapper.fromBookmarkRow(row)).thenReturn(mapped);

            SliceResponse<TrashItemRes> result = trashService.list(USER_ID, TrashType.BOOKMARK, 0, 10);

            assertThat(result.items()).hasSize(1);
            assertThat(result.items().getFirst().type()).isEqualTo(TrashType.BOOKMARK);

            verify(trashRepository, never()).countDeletedChildCollections(any(), any());
            verify(trashRepository, never()).countDeletedBookmarksInCollections(any(), any());
            verify(trashRepository, never()).countTaggedCountByTagIds(any(), any());
        }
    }

    @Nested
    @DisplayName("empty")
    class EmptyTest {
        @Test
        @DisplayName("전체 휴지통 비우기 시 컬렉션, 북마크, 태그를 모두 삭제한다")
        void empty_clears_all_trash_items_when_type_is_null() {
            trashService.empty(USER_ID, null);

            verify(collectionService).deleteAllFromTrash(USER_ID);
            verify(bookmarkService).deleteAllFromTrash(USER_ID);
            verify(tagService).deleteAllFromTrash(USER_ID);
        }

        @Test
        @DisplayName("타입별 휴지통 비우기 시 해당 타입만 삭제한다")
        void empty_clears_only_selected_type() {
            trashService.empty(USER_ID, TrashType.BOOKMARK);

            verify(bookmarkService).deleteAllFromTrash(USER_ID);
            verify(collectionService, never()).deleteAllFromTrash(any());
            verify(tagService, never()).deleteAllFromTrash(any());
        }
    }

    @Nested
    @DisplayName("restore")
    class RestoreTest {
        @Test
        @DisplayName("타입별 복원은 해당 서비스로 위임한다")
        void restore_restores_item_by_type() {
            trashService.restore(USER_ID, TrashType.COLLECTION, 10L);
            trashService.restore(USER_ID, TrashType.BOOKMARK, 20L);
            trashService.restore(USER_ID, TrashType.TAG, 30L);

            verify(collectionService).restoreFromTrash(USER_ID, 10L);
            verify(bookmarkService).restoreFromTrash(USER_ID, 20L);
            verify(tagService).restoreFromTrash(USER_ID, 30L);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {
        @Test
        @DisplayName("타입별 영구 삭제는 해당 서비스로 위임한다")
        void delete_deletes_item_by_type() {
            trashService.delete(USER_ID, TrashType.COLLECTION, 10L);
            trashService.delete(USER_ID, TrashType.BOOKMARK, 20L);
            trashService.delete(USER_ID, TrashType.TAG, 30L);

            verify(collectionService).deleteFromTrash(USER_ID, 10L);
            verify(bookmarkService).deleteFromTrash(USER_ID, 20L);
            verify(tagService).deleteFromTrash(USER_ID, 30L);
        }
    }

    @Nested
    @DisplayName("restoreMixedBulk")
    class RestoreMixedBulkTest {
        @Test
        @DisplayName("혼합 복원 시 타입별로 그룹핑해서 복원한다")
        void restore_mixed_bulk_groups_items_by_type_and_restores_them() {
            List<TrashBulkItem> items = List.of(
                    new TrashBulkItem(TrashType.COLLECTION, 10L),
                    new TrashBulkItem(TrashType.COLLECTION, 11L),
                    new TrashBulkItem(TrashType.BOOKMARK, 20L),
                    new TrashBulkItem(TrashType.BOOKMARK, 21L),
                    new TrashBulkItem(TrashType.TAG, 30L)
            );

            trashService.restoreMixedBulk(USER_ID, items);

            verify(collectionService).restoreFromTrash(USER_ID, 10L);
            verify(collectionService).restoreFromTrash(USER_ID, 11L);
            verify(bookmarkService).restoreFromTrashBulk(USER_ID, List.of(20L, 21L));
            verify(tagService).restoreFromTrashBulk(USER_ID, List.of(30L));
        }
    }

    @Nested
    @DisplayName("deleteMixedBulk")
    class DeleteMixedBulkTest {
        @Test
        @DisplayName("혼합 삭제 시 타입별로 그룹핑해서 삭제한다")
        void delete_mixed_bulk_groups_items_by_type_and_deletes_them() {
            List<TrashBulkItem> items = List.of(
                    new TrashBulkItem(TrashType.COLLECTION, 10L),
                    new TrashBulkItem(TrashType.BOOKMARK, 20L),
                    new TrashBulkItem(TrashType.BOOKMARK, 21L),
                    new TrashBulkItem(TrashType.TAG, 30L),
                    new TrashBulkItem(TrashType.TAG, 31L)
            );

            trashService.deleteMixedBulk(USER_ID, items);

            verify(collectionService).deleteFromTrashBulk(USER_ID, List.of(10L));
            verify(bookmarkService).deleteFromTrashBulk(USER_ID, List.of(20L, 21L));
            verify(tagService).deleteFromTrashBulk(USER_ID, List.of(30L, 31L));
        }
    }

    @Nested
    @DisplayName("restoreBulk")
    class RestoreBulkTest {
        @Test
        @DisplayName("컬렉션 다중 복원은 단건 복원을 반복 호출한다")
        void restore_bulk_restores_collections_one_by_one() {
            trashService.restoreBulk(USER_ID, TrashType.COLLECTION, List.of(10L, 11L));

            verify(collectionService).restoreFromTrash(USER_ID, 10L);
            verify(collectionService).restoreFromTrash(USER_ID, 11L);
            verify(bookmarkService, never()).restoreFromTrashBulk(any(), any());
            verify(tagService, never()).restoreFromTrashBulk(any(), any());
        }

        @Test
        @DisplayName("북마크 다중 복원은 한 번에 위임한다")
        void restore_bulk_restores_bookmarks_in_batch() {
            trashService.restoreBulk(USER_ID, TrashType.BOOKMARK, List.of(20L, 21L));

            verify(bookmarkService).restoreFromTrashBulk(USER_ID, List.of(20L, 21L));
            verify(collectionService, never()).restoreFromTrash(any(), any());
            verify(tagService, never()).restoreFromTrashBulk(any(), any());
        }

        @Test
        @DisplayName("태그 다중 복원은 한 번에 위임한다")
        void restore_bulk_restores_tags_in_batch() {
            trashService.restoreBulk(USER_ID, TrashType.TAG, List.of(30L, 31L));

            verify(tagService).restoreFromTrashBulk(USER_ID, List.of(30L, 31L));
            verify(collectionService, never()).restoreFromTrash(any(), any());
            verify(bookmarkService, never()).restoreFromTrashBulk(any(), any());
        }
    }

    private TrashCollectionRow collectionRow(Long id, String name, Instant deletedAt) {
        return new TrashCollectionRow(
                id,
                name,
                "📁",
                "부모",
                "📁",
                deletedAt
        );
    }

    private TrashBookmarkRow bookmarkRow(Long id, String title, Instant deletedAt) {
        return new TrashBookmarkRow(
                id,
                title,
                "https://example.com/" + id,
                "🔖",
                "컬렉션",
                "📁",
                deletedAt
        );
    }

    private Tag deletedTag(Long id, String name, Instant deletedAt) {
        return Tag.builder()
                .id(id)
                .name(name)
                .nameKey(name.toLowerCase(Locale.ROOT))
                .deletedAt(deletedAt)
                .build();
    }

    private TrashItemRes collectionItemRes(Long id, String title, Instant deletedAt) {
        return new TrashItemRes(
                TrashType.COLLECTION,
                id,
                title,
                null,
                "📁",
                "부모",
                "📁",
                deletedAt,
                null,
                null,
                null
        );
    }

    private TrashItemRes bookmarkItemRes(Long id, String title, Instant deletedAt) {
        return new TrashItemRes(
                TrashType.BOOKMARK,
                id,
                title,
                "https://example.com/" + id,
                "🔖",
                "컬렉션",
                "📁",
                deletedAt,
                null,
                null,
                null
        );
    }

    private TrashItemRes tagItemRes(Long id, String title, Instant deletedAt) {
        return new TrashItemRes(
                TrashType.TAG,
                id,
                title,
                null,
                null,
                null,
                null,
                deletedAt,
                null,
                null,
                null
        );
    }
}
