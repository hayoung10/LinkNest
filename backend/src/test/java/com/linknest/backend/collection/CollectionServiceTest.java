package com.linknest.backend.collection;

import com.linknest.backend.bookmark.BookmarkRepository;
import com.linknest.backend.collection.dto.request.CollectionCreateReq;
import com.linknest.backend.collection.dto.request.CollectionEmojiUpdateReq;
import com.linknest.backend.collection.dto.response.CollectionPositionRes;
import com.linknest.backend.collection.dto.response.CollectionRes;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.tag.TagService;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {
    private static final Long USER_ID = 1L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-03-28T00:00:00Z");

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private TagService tagService;

    private Clock clock;
    private CollectionService collectionService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        collectionService = new CollectionService(
                clock,
                collectionRepository,
                bookmarkRepository,
                userRepository,
                collectionMapper,
                tagService
        );
    }

    @Nested
    @DisplayName("create")
    class CreateTest {
        @Test
        @DisplayName("루트 컬렉션을 생성한다")
        void create_root_collection() {
            Long savedId = 100L;
            CollectionCreateReq req = new CollectionCreateReq("루트", "📁", null);

            User user = user(USER_ID);
            Collection mapped = collection(null, "루트", null, 0, false);
            Collection saved = collection(savedId, "루트", null, 2, false);

            CollectionRes expected = collectionRes(savedId, "루트", "📁", null, 2);

            when(collectionRepository.findMaxSortOrderByUserIdAndParentIsNull(USER_ID)).thenReturn(1);
            when(collectionMapper.toEntity(req)).thenReturn(mapped);
            when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
            when(collectionRepository.save(mapped)).thenReturn(saved);
            when(bookmarkRepository.countByCollectionIdAndDeletedAtIsNull(savedId)).thenReturn(0L);
            when(collectionRepository.countByUserIdAndParentId(USER_ID, savedId)).thenReturn(0L);
            when(collectionMapper.toResWithCount(saved, 0L, 0L)).thenReturn(expected);

            CollectionRes result = collectionService.create(USER_ID, req);

            assertThat(result).isSameAs(expected);
            assertThat(mapped.getUser()).isSameAs(user);
            assertThat(mapped.getParent()).isNull();
            assertThat(mapped.getSortOrder()).isEqualTo(2);
            verify(collectionRepository).save(mapped);
        }

        @Test
        @DisplayName("하위 컬렉션을 생성한다")
        void create_child_collection_under_parent() {
            Long parentId = 10L;
            Long savedId = 101L;
            CollectionCreateReq req = new CollectionCreateReq("자식", "📁", parentId);

            User user = user(USER_ID);
            Collection parent = collection(parentId, "부모", null, 0, false);
            Collection mapped = collection(null, "자식", null, 0, false);
            Collection saved = collection(savedId, "자식", parent, 3, false);

            CollectionRes expected = collectionRes(savedId, "자식", "📁", parentId, 3);

            when(collectionRepository.findByIdAndUserId(parentId, USER_ID)).thenReturn(Optional.of(parent));
            when(collectionRepository.findDepthByUserIdAndId(USER_ID, parentId)).thenReturn(2);
            when(collectionRepository.findMaxSortOrderByUserIdAndParentId(USER_ID, parentId)).thenReturn(2);
            when(collectionMapper.toEntity(req)).thenReturn(mapped);
            when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
            when(collectionRepository.save(mapped)).thenReturn(saved);
            when(bookmarkRepository.countByCollectionIdAndDeletedAtIsNull(savedId)).thenReturn(0L);
            when(collectionRepository.countByUserIdAndParentId(USER_ID, savedId)).thenReturn(0L);
            when(collectionMapper.toResWithCount(saved, 0L, 0L)).thenReturn(expected);

            CollectionRes result = collectionService.create(USER_ID, req);

            assertThat(result).isSameAs(expected);
            assertThat(mapped.getUser()).isSameAs(user);
            assertThat(mapped.getParent()).isSameAs(parent);
            assertThat(mapped.getSortOrder()).isEqualTo(3);
            verify(collectionRepository).save(mapped);
        }

        @Test
        @DisplayName("최대 depth를 초과하면 예외가 발생한다")
        void create_throws_when_max_depth_exceeded() {
            Long parentId = 10L;
            CollectionCreateReq req = new CollectionCreateReq("자식", "📁", parentId);
            Collection parent = collection(parentId, "부모", null, 0, false);

            when(collectionRepository.findByIdAndUserId(parentId, USER_ID)).thenReturn(Optional.of(parent));
            when(collectionRepository.findDepthByUserIdAndId(USER_ID, parentId)).thenReturn(5);

            assertThatThrownBy(() -> collectionService.create(USER_ID, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.COLLECTION_MAX_DEPTH_EXCEEDED));
        }

        @Test
        @DisplayName("소유하지 않은 부모 컬렉션이면 예외를 던진다")
        void create_throws_when_parent_not_owned() {
            Long parentId = 999L;
            CollectionCreateReq req = new CollectionCreateReq("자식", "📁", parentId);

            when(collectionRepository.findByIdAndUserId(parentId, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> collectionService.create(USER_ID, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.COLLECTION_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("move")
    class MoveTest {
        @Test
        @DisplayName("다른 부모로 정상 이동한다")
        void move_collection_to_another_parent() {
            Long collectionId = 20L;
            Long oldParentId = 1L;
            Long newParentId = 2L;

            Collection oldParent = collection(oldParentId, "기존 부모", null, 0, false);
            Collection newParent = collection(newParentId, "새 부모", null, 0, false);
            Collection target = collection(collectionId, "이동 대상", oldParent, 0, false);

            CollectionPositionRes expected = new CollectionPositionRes(collectionId, newParentId, 5);

            when(collectionRepository.findByIdAndUserId(collectionId, USER_ID)).thenReturn(Optional.of(target));
            when(collectionRepository.findByIdAndUserId(newParentId, USER_ID)).thenReturn(Optional.of(newParent));
            when(collectionRepository.findActiveSubtreeHeightByUserIdAndRootId(USER_ID, collectionId)).thenReturn(2);
            when(collectionRepository.findDepthByUserIdAndId(USER_ID, newParentId)).thenReturn(2);
            when(collectionRepository.findMaxSortOrderByUserIdAndParentId(USER_ID, newParentId)).thenReturn(4);
            when(collectionMapper.toPositionRes(target)).thenReturn(expected);

            CollectionPositionRes result = collectionService.move(USER_ID, collectionId, newParentId);

            assertThat(result).isSameAs(expected);
            assertThat(target.getParent()).isSameAs(newParent);
            assertThat(target.getSortOrder()).isEqualTo(5);
        }

        @Test
        @DisplayName("자기 자신을 부모로 지정하면 예외를 던진다")
        void move_throws_when_parent_is_self() {
            Long collectionId = 20L;
            Collection target = collection(collectionId, "대상", null, 0, false);

            when(collectionRepository.findByIdAndUserId(collectionId, USER_ID)).thenReturn(Optional.of(target));

            assertThatThrownBy(() -> collectionService.move(USER_ID, collectionId, collectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.COLLECTION_PARENT_SELF));
        }

        @Test
        @DisplayName("하위 컬렉션 아래로 이동하면 cycle 예외가 발생한다")
        void move_throws_when_creating_cycle() {
            Long collectionId = 20L;
            Long childId = 21L;

            Collection root = collection(collectionId, "루트", null, 0, false);
            Collection child = collection(childId, "자식", root, 0, false);

            when(collectionRepository.findByIdAndUserId(collectionId, USER_ID)).thenReturn(Optional.of(root));
            when(collectionRepository.findByIdAndUserId(childId, USER_ID)).thenReturn(Optional.of(child));

            assertThatThrownBy(() -> collectionService.move(USER_ID, collectionId, childId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.COLLECTION_CYCLE_DETECTED));
        }

        @Test
        @DisplayName("이동 후 최대 depth를 초과하면 예외가 발생한다")
        void move_throws_when_max_depth_exceeded() {
            Long collectionId = 20L;
            Long newParentId = 2L;

            Collection oldParent = collection(1L, "기존 부모", null, 0, false);
            Collection newParent = collection(newParentId, "새 부모", null, 0, false);
            Collection target = collection(collectionId, "이동 대상", oldParent, 0, false);

            when(collectionRepository.findByIdAndUserId(collectionId, USER_ID)).thenReturn(Optional.of(target));
            when(collectionRepository.findByIdAndUserId(newParentId, USER_ID)).thenReturn(Optional.of(newParent));
            when(collectionRepository.findActiveSubtreeHeightByUserIdAndRootId(USER_ID, collectionId)).thenReturn(3);
            when(collectionRepository.findDepthByUserIdAndId(USER_ID, newParentId)).thenReturn(3);

            assertThatThrownBy(() -> collectionService.move(USER_ID, collectionId, newParentId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.COLLECTION_MAX_DEPTH_EXCEEDED));
        }
    }

    @Nested
    @DisplayName("reorder")
    class ReorderTest {
        @Test
        @DisplayName("같은 부모 내에서 컬렉션 순서를 변경한다")
        void reorder_within_same_parent() {
            Long parentId = 1L;
            Long targetId = 11L;

            Collection parent = collection(parentId, "부모", null, 0, false);
            Collection first = collection(10L, "A", parent, 0, false);
            Collection second = collection(targetId, "B", parent, 1, false);
            Collection third = collection(12L, "C", parent, 2, false);

            CollectionPositionRes expected = new CollectionPositionRes(targetId, parentId, 0);

            when(collectionRepository.findByIdAndUserId(targetId, USER_ID)).thenReturn(Optional.of(second));
            when(collectionRepository.findAllByUserIdAndParentIdOrderBySortOrderAscCreatedAtAsc(USER_ID, parentId))
                    .thenReturn(new ArrayList<>(List.of(first, second, third)));
            when(collectionMapper.toPositionRes(second)).thenReturn(expected);

            CollectionPositionRes result = collectionService.reorder(USER_ID, targetId, 0);

            assertThat(result).isSameAs(expected);
            assertThat(second.getSortOrder()).isEqualTo(0);
            assertThat(first.getSortOrder()).isEqualTo(1);
            assertThat(third.getSortOrder()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {
        @Test
        @DisplayName("컬렉션 삭제로 하위(북마크, 태그)까지 휴지통 처리한다")
        void delete_soft_deletes_subtree_and_detaches_tags() {
            Long collectionId = 30L;
            List<Long> subtreeIds = List.of(30L, 31L, 32L);
            Set<Long> tagIds = Set.of(100L, 200L);

            Collection root = collection(collectionId, "루트", null, 0, false);

            when(collectionRepository.findByIdAndUserId(collectionId, USER_ID)).thenReturn(Optional.of(root));
            when(collectionRepository.findSubtreeIdsByUserIdAndRootId(USER_ID, collectionId)).thenReturn(subtreeIds);
            when(bookmarkRepository.findTagIdsByUserIdAndCollectionIds(USER_ID, subtreeIds)).thenReturn(tagIds);

            collectionService.delete(USER_ID, collectionId);

            verify(bookmarkRepository).softDeleteAllByCollectionIds(USER_ID, subtreeIds);
            verify(collectionRepository).softDeleteAllByIds(USER_ID, subtreeIds);

            ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
            verify(tagService).onTagsDetached(eq(tagIds), instantCaptor.capture());
            assertThat(instantCaptor.getValue()).isEqualTo(FIXED_INSTANT);
        }
    }

    @Nested
    @DisplayName("restoreFromTrash")
    class RestoreFromTrashTest {
        @Test
        @DisplayName("휴지통에서 컬렉션 복구로 하위(북마크, 태그)까지 함께 복구한다")
        void restore_from_trash_restores_bookmarks_and_tags() {
            Long parentId = 40L;
            Long collectionId = 41L;

            Collection parent = collection(parentId, "부모", null, 0, false);
            Collection deleted = collection(collectionId, "삭제 컬렉션", parent, 0, true);

            List<Long> subtreeIds = List.of(collectionId, 42L);
            Set<Long> tagIds = Set.of(301L, 302L);

            when(collectionRepository.findIncludingDeletedByIdAndUserId(collectionId, USER_ID))
                    .thenReturn(Optional.of(deleted));
            when(collectionRepository.findSubtreeIdsByUserIdAndRootId(USER_ID, collectionId))
                    .thenReturn(subtreeIds);
            when(collectionRepository.findIncludingDeletedByIdAndUserId(parentId, USER_ID))
                    .thenReturn(Optional.of(parent));
            when(collectionRepository.findSubtreeHeightIncludingDeletedByUserIdAndRootId(USER_ID, collectionId))
                    .thenReturn(2);
            when(collectionRepository.findDepthByUserIdAndId(USER_ID, parentId)).thenReturn(2);
            when(bookmarkRepository.findTagIdsByUserIdAndDeletedBookmarksInCollectionIds(USER_ID, subtreeIds))
                    .thenReturn(tagIds);

            collectionService.restoreFromTrash(USER_ID, collectionId);

            verify(collectionRepository).restoreDeletedByUserIdAndIdIn(USER_ID, subtreeIds);
            verify(bookmarkRepository).restoreDeletedByUserIdAndCollectionIds(USER_ID, subtreeIds);
            verify(tagService).onTagsAttached(tagIds);
            assertThat(deleted.getParent()).isSameAs(parent);
        }

        @Test
        @DisplayName("삭제되지 않은 컬렉션이면 복구를 수행하지 않는다")
        void restore_from_trash_returns_when_collection_is_not_deleted() {
            Long collectionId = 41L;
            Collection active = collection(collectionId, "활성 컬렉션", null, 0, false);

            when(collectionRepository.findIncludingDeletedByIdAndUserId(collectionId, USER_ID))
                    .thenReturn(Optional.of(active));

            collectionService.restoreFromTrash(USER_ID, collectionId);

            verify(collectionRepository, never()).findSubtreeIdsByUserIdAndRootId(any(), any());
            verify(collectionRepository, never()).restoreDeletedByUserIdAndIdIn(any(), any());
            verify(bookmarkRepository, never()).restoreDeletedByUserIdAndCollectionIds(any(), any());
            verify(tagService, never()).onTagsAttached(any());
        }

        @Test
        @DisplayName("복구할 subtree가 비어 있으면 예외를 던지다")
        void restore_from_trash_throws_when_subtree_is_empty() {
            Long collectionId = 41L;
            Collection deleted = collection(collectionId, "삭제 컬렉션", null, 0, true);

            when(collectionRepository.findIncludingDeletedByIdAndUserId(collectionId, USER_ID))
                    .thenReturn(Optional.of(deleted));
            when(collectionRepository.findSubtreeIdsByUserIdAndRootId(USER_ID, collectionId))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> collectionService.restoreFromTrash(USER_ID, collectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.COLLECTION_TRASH_RESTORE_FAILED));
        }

        @Test
        @DisplayName("부모가 삭제 상태면 루트 컬렉션으로 복구한다")
        void restore_from_trash_restores_as_root_when_parent_is_deleted() {
            Long parentId = 40L;
            Long collectionId = 41L;

            Collection deletedParent = collection(parentId, "삭제 부모", null, 0, true);
            Collection deleted = collection(collectionId, "삭제 컬렉션", deletedParent, 0, true);
            List<Long> subtreeIds = List.of(collectionId);

            when(collectionRepository.findIncludingDeletedByIdAndUserId(collectionId, USER_ID))
                    .thenReturn(Optional.of(deleted));
            when(collectionRepository.findSubtreeIdsByUserIdAndRootId(USER_ID, collectionId))
                    .thenReturn(subtreeIds);
            when(collectionRepository.findIncludingDeletedByIdAndUserId(parentId, USER_ID))
                    .thenReturn(Optional.of(deletedParent));
            when(collectionRepository.findSubtreeHeightIncludingDeletedByUserIdAndRootId(USER_ID, collectionId))
                    .thenReturn(1);
            when(collectionRepository.findDepthByUserIdAndId(USER_ID, parentId)).thenReturn(1);
            when(bookmarkRepository.findTagIdsByUserIdAndDeletedBookmarksInCollectionIds(USER_ID, subtreeIds))
                    .thenReturn(Set.of());

            collectionService.restoreFromTrash(USER_ID, collectionId);

            assertThat(deleted.getParent()).isNull();
            verify(collectionRepository).restoreDeletedByUserIdAndIdIn(USER_ID, subtreeIds);
            verify(bookmarkRepository).restoreDeletedByUserIdAndCollectionIds(USER_ID, subtreeIds);
        }

        @Test
        @DisplayName("복구 후 최대 depth를 초과하면 루트 컬렉션으로 복구한다")
        void restore_from_trash_restores_as_root_when_depth_exceeded() {
            Long parentId = 40L;
            Long collectionId = 41L;

            Collection parent = collection(parentId, "부모", null, 0, true);
            Collection deleted = collection(collectionId, "삭제 컬렉션", parent, 0, true);
            List<Long> subtreeIds = List.of(collectionId);

            when(collectionRepository.findIncludingDeletedByIdAndUserId(collectionId, USER_ID))
                    .thenReturn(Optional.of(deleted));
            when(collectionRepository.findSubtreeIdsByUserIdAndRootId(USER_ID, collectionId))
                    .thenReturn(subtreeIds);
            when(collectionRepository.findIncludingDeletedByIdAndUserId(parentId, USER_ID))
                    .thenReturn(Optional.of(parent));
            when(collectionRepository.findSubtreeHeightIncludingDeletedByUserIdAndRootId(USER_ID, collectionId))
                    .thenReturn(3);
            when(collectionRepository.findDepthByUserIdAndId(USER_ID, parentId)).thenReturn(3);
            when(bookmarkRepository.findTagIdsByUserIdAndDeletedBookmarksInCollectionIds(USER_ID, subtreeIds))
                    .thenReturn(Set.of());

            collectionService.restoreFromTrash(USER_ID, collectionId);

            assertThat(deleted.getParent()).isNull();
            verify(collectionRepository).restoreDeletedByUserIdAndIdIn(USER_ID, subtreeIds);
            verify(bookmarkRepository).restoreDeletedByUserIdAndCollectionIds(USER_ID, subtreeIds);
        }
    }

    @Nested
    @DisplayName("getOrCreateDefaultCollection")
    class GetOrCreateDefaultCollectionTest {
        @Test
        @DisplayName("활성 기본 컬렉션이 있으면 그대로 반환한다")
        void get_or_create_default_collection_returns_active_collection() {
            Collection active = collection(50L, "임시 보관함", null, 0, false);

            when(collectionRepository.findByUserIdAndParentIsNullAndName(USER_ID, "임시 보관함"))
                    .thenReturn(Optional.of(active));

            Collection result = collectionService.getOrCreateDefaultCollection(USER_ID);

            assertThat(result).isSameAs(active);
            verify(collectionRepository, never()).findIncludingDeletedDefault(any(), any());
            verify(collectionRepository, never()).save(any());
        }

        @Test
        @DisplayName("기본 컬렉션이 삭제된 상태면 복구 후 반환한다")
        void get_or_create_default_collection_restores_deleted_collection() {
            Collection oldParent = collection(99L, "예전 부모", null, 0, false);
            Collection deletedDefault = collection(50L, "임시 보관함", oldParent, 9, true);
            deletedDefault.setEmoji(null);

            when(collectionRepository.findByUserIdAndParentIsNullAndName(USER_ID, "임시 보관함"))
                    .thenReturn(Optional.empty());
            when(collectionRepository.findIncludingDeletedDefault(USER_ID, "임시 보관함"))
                    .thenReturn(Optional.of(deletedDefault));
            when(collectionRepository.findMaxSortOrderByUserIdAndParentIsNull(USER_ID)).thenReturn(2);

            Collection result = collectionService.getOrCreateDefaultCollection(USER_ID);

            assertThat(result).isSameAs(deletedDefault);
            assertThat(result.isDeleted()).isFalse();
            assertThat(result.getParent()).isNull();
            assertThat(result.getSortOrder()).isEqualTo(3);
            assertThat(result.getEmoji()).isEqualTo("📥");
            verify(collectionRepository, never()).save(any());
        }

        @Test
        @DisplayName("기본 컬렉션이 없으면 새로 생성한다")
        void get_or_create_default_collection_creates_when_missing() {
            User user = user(USER_ID);
            Collection saved = collection(60L, "임시 보관함", null, 0, false);

            when(collectionRepository.findByUserIdAndParentIsNullAndName(USER_ID, "임시 보관함"))
                    .thenReturn(Optional.empty());
            when(collectionRepository.findIncludingDeletedDefault(USER_ID, "임시 보관함"))
                    .thenReturn(Optional.empty());
            when(collectionRepository.findMaxSortOrderByUserIdAndParentIsNull(USER_ID)).thenReturn(null);
            when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
            when(collectionRepository.save(any(Collection.class))).thenReturn(saved);

            Collection result = collectionService.getOrCreateDefaultCollection(USER_ID);

            assertThat(result).isSameAs(saved);

            ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
            verify(collectionRepository).save(captor.capture());

            Collection created = captor.getValue();
            assertThat(created.getName()).isEqualTo("임시 보관함");
            assertThat(created.getEmoji()).isEqualTo("📥");
            assertThat(created.getParent()).isNull();
            assertThat(created.getSortOrder()).isEqualTo(0);
            assertThat(created.getUser()).isSameAs(user);
        }
    }

    @Nested
    @DisplayName("updateEmoji")
    class UpdateEmojiTest {
        @Test
        @DisplayName("빈 문자열 이모지는 null로 저장한다")
        void update_emoji_sets_null_when_blank() {
            Long collectionId = 70L;
            Collection collection = collection(collectionId, "컬렉션", null, 0, false);
            CollectionEmojiUpdateReq req = new CollectionEmojiUpdateReq("   ");

            CollectionRes expected = collectionRes(collectionId, "컬렉션", null, null, 0);

            when(collectionRepository.findByIdAndUserId(collectionId, USER_ID)).thenReturn(Optional.of(collection));
            when(collectionMapper.toRes(collection)).thenReturn(expected);

            CollectionRes result = collectionService.updateEmoji(USER_ID, collectionId, req);

            assertThat(result).isSameAs(expected);
            assertThat(collection.getEmoji()).isNull();
        }
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .build();
    }

    private Collection collection(Long id, String name, Collection parent, int sortOrder, boolean deleted) {
        Collection collection = Collection.builder()
                .id(id)
                .name(name)
                .emoji("📁")
                .parent(parent)
                .sortOrder(sortOrder)
                .createdAt(FIXED_INSTANT)
                .updatedAt(FIXED_INSTANT)
                .build();

        if(deleted) {
            collection.softDelete(FIXED_INSTANT);
        }
        return collection;
    }

    private CollectionRes collectionRes(Long id, String name, String emoji, Long parentId, int sortOrder) {
        return new CollectionRes(
                id,
                name,
                emoji,
                parentId,
                sortOrder,
                FIXED_INSTANT,
                FIXED_INSTANT,
                0L,
                0L
        );
    }
}
