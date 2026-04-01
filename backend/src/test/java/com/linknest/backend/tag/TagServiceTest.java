package com.linknest.backend.tag;

import com.linknest.backend.bookmark.BookmarkRepository;
import com.linknest.backend.bookmark.BookmarkTagRepository;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.tag.dto.*;
import com.linknest.backend.tag.mapper.TagMapper;
import com.linknest.backend.tag.mapper.TaggedBookmarkMapper;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.userpreferences.UserPreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {
    private static final Long USER_ID = 1L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-04-01T00:00:00Z");

    @Mock
    private TagRepository tagRepository;

    @Mock
    private BookmarkTagRepository bookmarkTagRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferencesService userPreferencesService;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TaggedBookmarkMapper taggedBookmarkMapper;

    private Clock clock;
    private TagService tagService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        tagService = new TagService(
                clock,
                tagRepository,
                bookmarkTagRepository,
                bookmarkRepository,
                userRepository,
                userPreferencesService,
                tagMapper,
                taggedBookmarkMapper
        );
    }

    @Nested
    @DisplayName("create")
    class CreateTest {
        @Test
        @DisplayName("같은 이름의 활성 태그가 있으면 예외가 발생한다")
        void create_throw_when_tag_with_same_name_already_exists() {
            TagCreateReq req = new TagCreateReq(" Java ");
            Tag existing = activeTag(10L, "Java", "java");

            when(tagRepository.findIncludingDeletedByUserIdAndNameKey(USER_ID, "java"))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> tagService.create(USER_ID, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.TAG_NAME_DUPLICATED));

            verify(tagRepository, never()).save(any());
            verify(tagRepository, never()).resetOrphanedAtByIds(any(), any());

        }

        @Test
        @DisplayName("같은 이름의 삭제된 태그가 있으면 복원한다")
        void create_restores_deleted_tag() {
            TagCreateReq req = new TagCreateReq(" Java ");
            Tag deleted = deletedTag(10L, "Java", "java");
            TagRes expected = tagRes(10L, "Java", 3L);

            when(tagRepository.findIncludingDeletedByUserIdAndNameKey(USER_ID, "java"))
                    .thenReturn(Optional.of(deleted));
            when(bookmarkTagRepository.countDistinctBookmarksByUserIdAndTagId(USER_ID, 10L))
                    .thenReturn(3L);
            when(tagMapper.toResWithCount(deleted, 3L)).thenReturn(expected);

            TagCreateResultRes result = tagService.create(USER_ID, req);

            assertThat(result.res()).isSameAs(expected);
            assertThat(result.restored()).isTrue();
            assertThat(deleted.isDeleted()).isFalse();
            assertThat(deleted.getName()).isEqualTo("Java");

            verify(tagRepository).resetOrphanedAtByIds(List.of(10L), FIXED_INSTANT);
            verify(tagRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("rename")
    class RenameTest {
        @Test
        @DisplayName("정규화 후 같은 이름이면 표시 이름만 변경한다")
        void rename_updates_display_name_when_normalized_name_is_same() {
            Long tagId = 10L;
            TagUpdateReq req = new TagUpdateReq(" Java ");
            Tag tag = activeTag(tagId, "java", "java");
            TagRes expected = tagRes(tagId, "Java", 2L);

            when(tagRepository.findByIdAndUserId(tagId, USER_ID)).thenReturn(Optional.of(tag));
            when(bookmarkTagRepository.countDistinctBookmarksByUserIdAndTagId(USER_ID, tagId))
                    .thenReturn(2L);
            when(tagMapper.toResWithCount(tag, 2L)).thenReturn(expected);

            TagRes result = tagService.rename(USER_ID, tagId, req);

            assertThat(result).isSameAs(expected);
            assertThat(tag.getName()).isEqualTo("Java");
            assertThat(tag.getNameKey()).isEqualTo("java");

            verify(tagRepository, never()).findIncludingDeletedByUserIdAndNameKey(any(), any());
        }

        @Test
        @DisplayName("다른 활성 태그와 이름이 겹치면 예외가 발생한다")
        void rename_throw_when_name_conflicts_with_other_tag() {
            Long tagId = 10L;
            TagUpdateReq req = new TagUpdateReq("Spring");
            Tag current = activeTag(tagId, "Java", "java");
            Tag another = activeTag(20L, "Spring", "spring");

            when(tagRepository.findByIdAndUserId(tagId, USER_ID)).thenReturn(Optional.of(current));
            when(tagRepository.findIncludingDeletedByUserIdAndNameKey(USER_ID, "spring"))
                    .thenReturn(Optional.of(another));

            assertThatThrownBy(() -> tagService.rename(USER_ID, tagId, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.TAG_NAME_DUPLICATED));
        }
    }

    @Nested
    @DisplayName("merge")
    class MergeTest {
        @Test
        @DisplayName("중복 북마크 연결을 제거하면서 대상 태그로 병합한다")
        void merge_moves_bookmarks_to_target_and_removes_duplicates() {
            Long sourceTagId = 10L;
            Long targetTagId = 20L;

            Tag source = activeTag(sourceTagId, "Java", "java");
            Tag target = activeTag(targetTagId, "Spring", "spring");
            Tag targetRef = activeTag(targetTagId, "Spring", "spring");

            when(tagRepository.findByIdAndUserId(sourceTagId, USER_ID)).thenReturn(Optional.of(source));
            when(tagRepository.findByIdAndUserId(targetTagId, USER_ID)).thenReturn(Optional.of(target));
            when(bookmarkTagRepository.findAllBookmarkIdsByUserIdAndTagId(USER_ID, sourceTagId))
                    .thenReturn(List.of(101L, 102L, 103L));
            when(bookmarkTagRepository.findBookmarkIdsByUserIdAndTagIdAndBookmarkIdIn(
                    USER_ID, targetTagId, List.of(101L, 102L, 103L)))
                    .thenReturn(List.of(102L));
            when(tagRepository.getReferenceById(targetTagId)).thenReturn(targetRef);

            tagService.merge(USER_ID, sourceTagId, new TagMergeReq(targetTagId));

            verify(bookmarkTagRepository)
                    .deleteByUserIdAndTagIdAndBookmarkIdIn(USER_ID, sourceTagId, List.of(102L));
            verify(bookmarkTagRepository)
                    .replaceTagOnBookmarks(USER_ID, sourceTagId, targetRef, List.of(101L, 102L, 103L));
        }
    }

    @Nested
    @DisplayName("replaceTagOnBookmarks")
    class ReplaceTest {
        @Test
        @DisplayName("중복 연결을 제외한 북마크만 대상 태그로 교체한다")
        void replace_updates_bookmarks_and_skips_duplicates() {
            Long sourceTagId = 10L;
            Long targetTagId = 20L;

            Tag source = activeTag(sourceTagId, "Java", "java");
            Tag target = activeTag(targetTagId, "Spring", "spring");
            Tag targetRef = activeTag(targetTagId, "Spring", "spring");

            TagReplaceReq req = new TagReplaceReq(List.of(101L, 102L, 103L), targetTagId);

            when(tagRepository.findByIdAndUserId(sourceTagId, USER_ID)).thenReturn(Optional.of(source));
            when(tagRepository.findByIdAndUserId(targetTagId, USER_ID)).thenReturn(Optional.of(target));
            when(bookmarkRepository.countByUserIdAndIdIn(USER_ID, List.of(101L, 102L, 103L))).thenReturn(3L);
            when(bookmarkTagRepository.findBookmarkIdsByUserIdAndTagIdAndBookmarkIdIn(
                    USER_ID, targetTagId, List.of(101L, 102L, 103L)))
                    .thenReturn(List.of(102L));
            when(tagRepository.getReferenceById(targetTagId)).thenReturn(targetRef);

            tagService.replaceTagOnBookmarks(USER_ID, sourceTagId, req);

            verify(bookmarkTagRepository)
                    .deleteByUserIdAndTagIdAndBookmarkIdIn(USER_ID, sourceTagId, List.of(102L));
            verify(bookmarkTagRepository)
                    .replaceTagOnBookmarks(USER_ID, sourceTagId, targetRef, List.of(101L, 103L));
        }

        @Test
        @DisplayName("태그 교체 후 원본 태그와 대상 태그의 상태를 갱신한다")
        void replace_updates_tag_states() {
            Long sourceTagId = 10L;
            Long targetTagId = 20L;

            Tag source = activeTag(sourceTagId, "Java", "java");
            Tag target = activeTag(targetTagId, "Spring", "spring");
            Tag targetRef = activeTag(targetTagId, "Spring", "spring");

            TagReplaceReq req = new TagReplaceReq(List.of(101L, 102L), targetTagId);

            when(tagRepository.findByIdAndUserId(sourceTagId, USER_ID)).thenReturn(Optional.of(source));
            when(tagRepository.findByIdAndUserId(targetTagId, USER_ID)).thenReturn(Optional.of(target));
            when(bookmarkRepository.countByUserIdAndIdIn(USER_ID, List.of(101L, 102L))).thenReturn(2L);
            when(bookmarkTagRepository.findBookmarkIdsByUserIdAndTagIdAndBookmarkIdIn(
                    USER_ID, targetTagId, List.of(101L, 102L)))
                    .thenReturn(List.of());
            when(tagRepository.getReferenceById(targetTagId)).thenReturn(targetRef);

            tagService.replaceTagOnBookmarks(USER_ID, sourceTagId, req);

            verify(tagRepository).setOrphanedAtIfUnusedByIds(List.of(sourceTagId), FIXED_INSTANT);
            verify(tagRepository).clearOrphanedAtByIds(List.of(targetTagId));
        }
    }

    @Nested
    @DisplayName("restoreFromTrash")
    class RestoreFromTrashTest {
        @Test
        @DisplayName("삭제된 태그를 복원한다")
        void restore_restores_deleted_tag() {
            Long tagId = 10L;
            Tag deleted = deletedTag(tagId, "Java", "java");

            when(tagRepository.findIncludingDeletedByIdAndUserId(tagId, USER_ID)).thenReturn(Optional.of(deleted));
            when(tagRepository.findByUserIdAndNameKey(USER_ID, "java")).thenReturn(Optional.empty());

            tagService.restoreFromTrash(USER_ID, tagId);

            assertThat(deleted.isDeleted()).isFalse();
            verify(tagRepository).resetOrphanedAtByIds(List.of(tagId), FIXED_INSTANT);
        }

        @Test
        @DisplayName("복원하려는 태그와 같은 이름의 활성 태그가 있으면 예외가 발생한다")
        void restore_throw_when_active_tag_with_same_name_exists() {
            Long tagId = 10L;
            Tag deleted = deletedTag(tagId, "Java", "java");
            Tag active = activeTag(20L, "Java", "java");

            when(tagRepository.findIncludingDeletedByIdAndUserId(tagId, USER_ID)).thenReturn(Optional.of(deleted));
            when(tagRepository.findByUserIdAndNameKey(USER_ID, "java")).thenReturn(Optional.of(active));

            assertThatThrownBy(() -> tagService.restoreFromTrash(USER_ID, tagId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.TAG_NAME_DUPLICATED));

            assertThat(deleted.isDeleted()).isTrue();
            verify(tagRepository, never()).resetOrphanedAtByIds(any(), any());
        }
    }

    @Nested
    @DisplayName("getOrCreateByName")
    class GetOrCreateByNameTest {
        @Test
        @DisplayName("삭제된 태그가 있으면 복원해서 사용한다")
        void get_or_create_restores_deleted_tags() {
            Tag deleted = deletedTag(10L, "Java", "java");
            deleted.setOrphanedAt(FIXED_INSTANT.minusSeconds(60));

            when(tagRepository.findAllIncludingDeletedByUserIdAndNameKeyIn(USER_ID, Set.of("java")))
                    .thenReturn(List.of(deleted));

            Set<Tag> result = tagService.getOrCreateByName(USER_ID, List.of(" Java "));

            assertThat(result).containsExactly(deleted);
            assertThat(deleted.isDeleted()).isFalse();
            assertThat(deleted.getName()).isEqualTo("Java");
            assertThat(deleted.getNameKey()).isEqualTo("java");
            verify(tagRepository).clearOrphanedAtByIds(List.of(10L));
            verify(tagRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("태그 개수 제한을 초과하면 예외가 발생한다")
        void get_or_create_throw_when_tag_limit_exceeded() {
            assertThatThrownBy(() ->
                    tagService.getOrCreateByName(USER_ID, List.of("java", "spring", "mysql", "redis")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.TAG_LIMIT_EXCEEDED));

            verify(tagRepository, never()).findAllIncludingDeletedByUserIdAndNameKeyIn(any(), any());
            verify(tagRepository, never()).saveAll(any());
        }
    }

    private Tag activeTag(Long id, String name, String nameKey) {
        return Tag.builder()
                .id(id)
                .name(name)
                .nameKey(nameKey)
                .user(user(USER_ID))
                .createdAt(FIXED_INSTANT)
                .updatedAt(FIXED_INSTANT)
                .build();
    }

    private Tag deletedTag(Long id, String name, String nameKey) {
        Tag tag = activeTag(id, name, nameKey);
        tag.softDelete(FIXED_INSTANT.minusSeconds(60));
        return tag;
    }

    private TagRes tagRes(Long id, String name, long bookmarkCount) {
        return new TagRes(
                id,
                name,
                FIXED_INSTANT,
                FIXED_INSTANT,
                bookmarkCount
        );
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .build();
    }
}
