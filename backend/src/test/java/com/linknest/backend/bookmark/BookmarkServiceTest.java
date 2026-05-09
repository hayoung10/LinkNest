package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.Bookmark.AutoImageStatus;
import com.linknest.backend.bookmark.Bookmark.ImageMode;
import com.linknest.backend.bookmark.dto.BookmarkCreateReq;
import com.linknest.backend.bookmark.dto.BookmarkRes;
import com.linknest.backend.bookmark.dto.BookmarkUpdateReq;
import com.linknest.backend.bookmark.event.BookmarkAutoImageRequestedEvent;
import com.linknest.backend.collection.Collection;
import com.linknest.backend.collection.CollectionRepository;
import com.linknest.backend.collection.CollectionService;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.storage.Storage;
import com.linknest.backend.storage.UploadProperties;
import com.linknest.backend.tag.Tag;
import com.linknest.backend.tag.TagService;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.userpreferences.UserPreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {
    private static final Long USER_ID = 1L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-03-30T00:00:00Z");

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferencesService userPreferencesService;

    @Mock
    private TagService tagService;

    @Mock
    private CollectionService collectionService;

    @Mock
    private BookmarkMapper bookmarkMapper;

    @Mock
    private Storage storage;

    @Mock
    private UploadProperties uploadProperties;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private BookmarkService bookmarkService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        bookmarkService = new BookmarkService(
                clock,
                bookmarkRepository,
                collectionRepository,
                userRepository,
                userPreferencesService,
                tagService,
                collectionService,
                bookmarkMapper,
                storage,
                uploadProperties,
                eventPublisher
        );
    }

    @Nested
    @DisplayName("create")
    class CreateTest {
        @Test
        @DisplayName("imageMode가 없으면 AUTO로 저장하고 자동 이미지 요청을 발행한다")
        void create_sets_auto_mode_and_publishes_event() {
            Long collectionId = 10L;
            Long bookmarkId = 100L;

            BookmarkCreateReq req = new BookmarkCreateReq(
                    collectionId,
                    "https://example.com",
                    "title",
                    "desc",
                    "🔖",
                    null,
                    List.of("tag1", "tag2")
            );

            User user = user(USER_ID);
            Collection ownedCollection = ownedCollection(USER_ID);

            Tag tag1 = tag(1L);
            Tag tag2 = tag(2L);
            Set<Tag> tags = Set.of(tag1, tag2);

            Bookmark newBookmark = bookmark(null, user, ownedCollection, "https://example.com");
            newBookmark.setImageMode(null);
            newBookmark.setCustomImageUrl("should-clear");
            newBookmark.setAutoImageUrl("should-clear");

            Bookmark savedBookmark = bookmark(bookmarkId, user, ownedCollection, "https://example.com");
            savedBookmark.setImageMode(ImageMode.AUTO);
            savedBookmark.setAutoImageStatus(AutoImageStatus.PENDING);

            BookmarkRes expected = bookmarkRes(
                    bookmarkId,
                    collectionId,
                    "https://example.com",
                    ImageMode.AUTO,
                    AutoImageStatus.PENDING
            );

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(ownedCollection));
            when(bookmarkMapper.toEntity(req)).thenReturn(newBookmark);
            when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
            when(bookmarkRepository.save(newBookmark)).thenReturn(savedBookmark);
            when(bookmarkMapper.toRes(savedBookmark)).thenReturn(expected);
            when(tagService.getOrCreateByName(USER_ID, req.tags())).thenReturn(tags);

            BookmarkRes result = bookmarkService.create(USER_ID, req);

            assertThat(result).isSameAs(expected);
            assertThat(newBookmark.getUser()).isSameAs(user);
            assertThat(newBookmark.getCollection()).isSameAs(ownedCollection);
            assertThat(newBookmark.getImageMode()).isEqualTo(ImageMode.AUTO);
            assertThat(newBookmark.getCustomImageUrl()).isNull();
            assertThat(newBookmark.getAutoImageUrl()).isNull();
            assertThat(newBookmark.getAutoImageStatus()).isEqualTo(AutoImageStatus.PENDING);

            assertThat(savedBookmark.getBookmarkTags()).hasSize(2);
            Set<Long> tagIds = savedBookmark.getBookmarkTags().stream()
                    .map(BookmarkTag::getTag)
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            assertThat(tagIds).containsExactlyInAnyOrder(1L, 2L);

            ArgumentCaptor<BookmarkAutoImageRequestedEvent> eventCaptor =
                    ArgumentCaptor.forClass(BookmarkAutoImageRequestedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            BookmarkAutoImageRequestedEvent event = eventCaptor.getValue();
            assertThat(event.userId()).isEqualTo(USER_ID);
            assertThat(event.bookmarkId()).isEqualTo(bookmarkId);
            assertThat(event.url()).isEqualTo("https://example.com");
        }

        @Test
        @DisplayName("생성 시 imageMode가 CUSTOM이면 예외가 발생한다")
        void create_throws_when_image_mode_is_custom() {
            Long collectionId = 10L;

            BookmarkCreateReq req = new BookmarkCreateReq(
                    collectionId,
                    "https://example.com",
                    "title",
                    "desc",
                    null,
                    ImageMode.CUSTOM,
                    List.of()
            );

            Collection ownedCollection = ownedCollection(USER_ID);
            Bookmark newBookmark = bookmark(null, user(USER_ID), ownedCollection, "https://example.com");
            newBookmark.setImageMode(ImageMode.CUSTOM);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(ownedCollection));
            when(bookmarkMapper.toEntity(req)).thenReturn(newBookmark);
            when(userRepository.getReferenceById(USER_ID)).thenReturn(user(USER_ID));

            assertThatThrownBy(() -> bookmarkService.create(USER_ID, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_IMAGE_MODE));
        }

        @Test
        @DisplayName("소유하지 않은 컬렉션에 북마크 생성 시 예외가 발생한다")
        void create_throws_when_collection_is_not_owned() {
            Long collectionId = 10L;
            BookmarkCreateReq req = new BookmarkCreateReq(
                    collectionId,
                    "https://example.com",
                    null,
                    null,
                    null,
                    null,
                    List.of()
            );

            Collection notOwnedCollection = ownedCollection(999L);

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(notOwnedCollection));

            assertThatThrownBy(() -> bookmarkService.create(USER_ID, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ACCESS_DENIED));
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {
        @Test
        @DisplayName("수정 후 URL이 비어 있으면 예외가 발생한다")
        void update_throws_when_url_is_blank() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://before.com");
            bookmark.setImageMode(ImageMode.AUTO);

            BookmarkUpdateReq req = new BookmarkUpdateReq("   ", "new", "desc", List.of());

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            doAnswer(invocation -> {
                Bookmark target = invocation.getArgument(1);
                target.setUrl("   ");
                return null;
            }).when(bookmarkMapper).updateFromDto(eq(req), eq(bookmark));

            assertThatThrownBy(() -> bookmarkService.update(USER_ID, bookmarkId, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_BOOKMARK_URL));
        }

        @Test
        @DisplayName("AUTO 모드에서 URL이 변경되면 자동 이미지를 다시 요청한다")
        void update_auto_mode_with_changed_url_resets_auto_image_and_publishes_event() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://before.com");
            bookmark.setImageMode(ImageMode.AUTO);
            bookmark.setAutoImageUrl("https://old-auto.com");
            bookmark.setAutoImageStatus(AutoImageStatus.SUCCESS);

            BookmarkUpdateReq req = new BookmarkUpdateReq(
                    "https://after.com",
                    "new-title",
                    "new-desc",
                    null
            );

            BookmarkRes expected = bookmarkRes(
                    bookmarkId,
                    10L,
                    "https://after.com",
                    ImageMode.AUTO,
                    AutoImageStatus.PENDING
            );

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            doAnswer(invocation -> {
                Bookmark target = invocation.getArgument(1);
                target.setUrl("https://after.com");
                target.setTitle("new-title");
                target.setDescription("new-desc");
                return null;
            }).when(bookmarkMapper).updateFromDto(eq(req), eq(bookmark));
            when(bookmarkMapper.toRes(bookmark)).thenReturn(expected);

            BookmarkRes result = bookmarkService.update(USER_ID, bookmarkId, req);

            assertThat(result).isSameAs(expected);
            assertThat(bookmark.getAutoImageUrl()).isNull();
            assertThat(bookmark.getAutoImageStatus()).isEqualTo(AutoImageStatus.PENDING);

            ArgumentCaptor<BookmarkAutoImageRequestedEvent> eventCaptor =
                    ArgumentCaptor.forClass(BookmarkAutoImageRequestedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            BookmarkAutoImageRequestedEvent event = eventCaptor.getValue();
            assertThat(event.userId()).isEqualTo(USER_ID);
            assertThat(event.bookmarkId()).isEqualTo(bookmarkId);
            assertThat(event.url()).isEqualTo("https://after.com");

            verify(tagService, never()).onTagsDetached(any(), any());
            verify(tagService, never()).onTagsAttached(any());
        }

        @Test
        @DisplayName("수정 시 태그 변경 내용을 반영한다")
        void update_updates_tags() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(
                    bookmarkId,
                    user(USER_ID),
                    collectionRef(),
                    "https://example.com"
            );
            bookmark.setImageMode(ImageMode.NONE);
            bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, tag(1L)));
            bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, tag(2L)));

            BookmarkUpdateReq req = new BookmarkUpdateReq(
                    "https://example.com",
                    "title",
                    "desc",
                    List.of("old2", "new3")
            );

            Tag tag2 = tag(2L);
            Tag tag3 = tag(3L);
            Set<Tag> tags = Set.of(tag2, tag3);

            BookmarkRes expected = bookmarkRes(
                    bookmarkId,
                    10L,
                    "https://example.com",
                    ImageMode.NONE,
                    null
            );

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            doAnswer(invocation -> null).when(bookmarkMapper).updateFromDto(eq(req), eq(bookmark));
            when(tagService.getOrCreateByName(USER_ID, req.tags())).thenReturn(tags);
            when(bookmarkMapper.toRes(bookmark)).thenReturn(expected);

            bookmarkService.update(USER_ID, bookmarkId, req);

            assertThat(bookmark.getBookmarkTags()).hasSize(2);
            Set<Long> currentTagIds = bookmark.getBookmarkTags().stream()
                    .map(BookmarkTag::getTag)
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            assertThat(currentTagIds).containsExactlyInAnyOrder(2L, 3L);

            verify(tagService).onTagsDetached(eq(Set.of(1L)), eq(FIXED_INSTANT));
            verify(tagService).onTagsAttached(eq(Set.of(3L)));
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {
        @Test
        @DisplayName("북마크를 휴지통으로 이동하고 연결 태그를 정리한다")
        void delete_moves_bookmark_to_trash_and_detaches_tags() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, tag(1L)));
            bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, tag(2L)));

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));

            bookmarkService.delete(USER_ID, bookmarkId);

            assertThat(bookmark.isDeleted()).isTrue();

            ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
            verify(tagService).onTagsDetached(eq(Set.of(1L, 2L)), instantCaptor.capture());
            assertThat(instantCaptor.getValue()).isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("이미 휴지통에 있는 북마크면 그대로 종료한다")
        void delete_returns_when_bookmark_is_already_in_trash() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.softDelete(FIXED_INSTANT.minusSeconds(60));

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));

            bookmarkService.delete(USER_ID, bookmarkId);

            verify(tagService, never()).onTagsDetached(any(), any());
        }
    }

    @Nested
    @DisplayName("uploadCover")
    class UploadCoverTest {
        @Test
        @DisplayName("업로드 파일이 비어 있으면 예외가 발생한다")
        void upload_cover_throws_when_file_is_empty() {
            MockMultipartFile emptyFile = new MockMultipartFile("cover", new byte[0]);

            assertThatThrownBy(() -> bookmarkService.uploadCover(USER_ID, 100L, emptyFile))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.FILE_EMPTY));
        }

        @Test
        @DisplayName("기존 커버를 삭제하고 새 커버 업로드 후 CUSTOM 모드로 변경한다")
        void upload_cover_deletes_old_cover_and_sets_custom_mode() {
            String bookmarkCoverDir = "uploads/bookmark-covers";

            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.setCustomImageUrl("https://old-cover.com");
            bookmark.setImageMode(ImageMode.AUTO);
            bookmark.setAutoImageStatus(AutoImageStatus.SUCCESS);

            MockMultipartFile file = new MockMultipartFile("cover", "cover.png", "image/png", "hello".getBytes());

            BookmarkRes expected = bookmarkRes(bookmarkId, 10L, "https://example.com", ImageMode.CUSTOM, null);

            when(uploadProperties.bookmarkCoverDir()).thenReturn(bookmarkCoverDir);
            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            when(storage.upload(bookmarkCoverDir, file)).thenReturn("https://new-cover.com");
            when(bookmarkMapper.toRes(bookmark)).thenReturn(expected);

            BookmarkRes result = bookmarkService.uploadCover(USER_ID, bookmarkId, file);

            assertThat(result).isSameAs(expected);
            assertThat(bookmark.getCustomImageUrl()).isEqualTo("https://new-cover.com");
            assertThat(bookmark.getImageMode()).isEqualTo(ImageMode.CUSTOM);
            assertThat(bookmark.getAutoImageStatus()).isNull();

            verify(storage).delete("https://old-cover.com");
            verify(storage).upload(bookmarkCoverDir, file);
        }
    }

    @Nested
    @DisplayName("removeCover")
    class RemoveCoverTest {
        @Test
        @DisplayName("커버를 삭제한 뒤 자동 이미지가 없으면 다시 요청한다")
        void remove_cover_sets_auto_mode_and_publishes_event_when_auto_image_missing() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.setCustomImageUrl("https://custom-cover.com");
            bookmark.setImageMode(ImageMode.CUSTOM);
            bookmark.setAutoImageUrl(null);

            BookmarkRes expected = bookmarkRes(bookmarkId, 10L, "https://example.com", ImageMode.AUTO, AutoImageStatus.PENDING);

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            when(bookmarkMapper.toRes(bookmark)).thenReturn(expected);

            BookmarkRes result = bookmarkService.removeCover(USER_ID, bookmarkId);

            assertThat(result).isSameAs(expected);
            assertThat(bookmark.getImageMode()).isEqualTo(ImageMode.AUTO);
            assertThat(bookmark.getCustomImageUrl()).isNull();
            assertThat(bookmark.getAutoImageStatus()).isEqualTo(AutoImageStatus.PENDING);

            verify(storage).delete("https://custom-cover.com");

            ArgumentCaptor<BookmarkAutoImageRequestedEvent> eventCaptor =
                    ArgumentCaptor.forClass(BookmarkAutoImageRequestedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            assertThat(eventCaptor.getValue().bookmarkId()).isEqualTo(bookmarkId);
        }
    }

    @Nested
    @DisplayName("updateImageMode")
    class UpdateImageModeTest {
        @Test
        @DisplayName("imageMode가 null이면 예외가 발생한다")
        void update_image_mode_throws_when_null() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));

            assertThatThrownBy(() -> bookmarkService.updateImageMode(USER_ID, bookmarkId, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_IMAGE_MODE));
        }

        @Test
        @DisplayName("imageMode를 CUSTOM으로 직접 변경하면 예외가 발생한다")
        void update_image_mode_throws_when_custom() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));

            assertThatThrownBy(() -> bookmarkService.updateImageMode(USER_ID, bookmarkId, ImageMode.CUSTOM))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_IMAGE_MODE));
        }

        @Test
        @DisplayName("AUTO 모드로 변경할 때 자동 이미지가 없으면 다시 요청한다")
        void update_image_mode_to_auto_sets_pending_and_publishes_event_when_auto_image_missing() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.setImageMode(ImageMode.NONE);
            bookmark.setAutoImageUrl(null);

            BookmarkRes expected = bookmarkRes(bookmarkId, 10L, "https://example.com", ImageMode.AUTO, AutoImageStatus.PENDING);

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            when(bookmarkMapper.toRes(bookmark)).thenReturn(expected);

            BookmarkRes result = bookmarkService.updateImageMode(USER_ID, bookmarkId, ImageMode.AUTO);

            assertThat(result).isSameAs(expected);
            assertThat(bookmark.getImageMode()).isEqualTo(ImageMode.AUTO);
            assertThat(bookmark.getAutoImageStatus()).isEqualTo(AutoImageStatus.PENDING);

            verify(eventPublisher).publishEvent(any(BookmarkAutoImageRequestedEvent.class));
        }

        @Test
        @DisplayName("AUTO 모드로 변경할 때 자동 이미지가 있으면 성공 상태로 변경한다")
        void update_image_mode_to_auto_sets_success_when_auto_image_exists() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.setImageMode(ImageMode.NONE);
            bookmark.setAutoImageUrl("https://auto-image.com");

            BookmarkRes expected = bookmarkRes(bookmarkId, 10L, "https://example.com", ImageMode.AUTO, AutoImageStatus.SUCCESS);

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            when(bookmarkMapper.toRes(bookmark)).thenReturn(expected);

            BookmarkRes result = bookmarkService.updateImageMode(USER_ID, bookmarkId, ImageMode.AUTO);

            assertThat(result).isSameAs(expected);
            assertThat(bookmark.getImageMode()).isEqualTo(ImageMode.AUTO);
            assertThat(bookmark.getAutoImageStatus()).isEqualTo(AutoImageStatus.SUCCESS);

            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("NONE 모드로 변경하면 자동 이미지 정보를 제거한다")
        void update_image_mode_to_none_clears_auto_image_fields() {
            Long bookmarkId = 100L;

            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.setImageMode(ImageMode.AUTO);
            bookmark.setAutoImageUrl("https://auto-image.com");
            bookmark.setAutoImageStatus(AutoImageStatus.SUCCESS);

            BookmarkRes expected = bookmarkRes(
                    bookmarkId,
                    10L,
                    "https://example.com",
                    ImageMode.NONE,
                    null
            );

            when(bookmarkRepository.findByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            when(bookmarkMapper.toRes(bookmark)).thenReturn(expected);

            BookmarkRes result = bookmarkService.updateImageMode(USER_ID, bookmarkId, ImageMode.NONE);

            assertThat(result).isSameAs(expected);
            assertThat(bookmark.getImageMode()).isEqualTo(ImageMode.NONE);
            assertThat(bookmark.getAutoImageUrl()).isNull();
            assertThat(bookmark.getAutoImageStatus()).isNull();

            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("restoreFromTrash")
    class RestoreFromTrashTest {
        @Test
        @DisplayName("삭제되지 않은 북마크면 그대로 종료한다")
        void restore_from_trash_returns_when_bookmark_is_not_deleted() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");

            when(bookmarkRepository.findIncludingDeletedByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));

            bookmarkService.restoreFromTrash(USER_ID, bookmarkId);

            verify(bookmarkRepository, never()).restoreDeletedByUserIdAndIdIn(any(), any());
            verify(collectionService, never()).getOrCreateDefaultCollection(any());
            verify(tagService, never()).onTagsAttached(any());
        }

        @Test
        @DisplayName("부모 컬렉션이 삭제된 북마크는 기본 컬렉션으로 복구한다")
        void restore_from_trash_moves_to_default_collection_when_parent_collection_deleted() {
            Long bookmarkId = 100L;
            Long defaultCollectionId = 999L;

            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.softDelete(FIXED_INSTANT.minusSeconds(60));

            Collection defaultCollection = collectionWithId(defaultCollectionId);

            when(bookmarkRepository.findIncludingDeletedByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            when(bookmarkRepository.countDeletedParentCollectionByUserIdAndIds(USER_ID, List.of(bookmarkId))).thenReturn(1L);
            when(collectionService.getOrCreateDefaultCollection(USER_ID)).thenReturn(defaultCollection);
            when(bookmarkRepository.findTagIdsByUserIdAndBookmarkIds(USER_ID, List.of(bookmarkId))).thenReturn(Set.of());

            bookmarkService.restoreFromTrash(USER_ID, bookmarkId);

            verify(collectionService).getOrCreateDefaultCollection(USER_ID);
            verify(bookmarkRepository).moveDeletedToDefaultIfParentDeleted(USER_ID, List.of(bookmarkId), defaultCollectionId);
            verify(bookmarkRepository).restoreDeletedByUserIdAndIdIn(USER_ID, List.of(bookmarkId));
        }

        @Test
        @DisplayName("휴지통에서 복구할 때 관련 태그도 다시 반영한다")
        void restore_from_trash_attaches_tags_after_restore() {
            Long bookmarkId = 100L;

            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.softDelete(FIXED_INSTANT.minusSeconds(60));

            when(bookmarkRepository.findIncludingDeletedByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            when(bookmarkRepository.countDeletedParentCollectionByUserIdAndIds(USER_ID, List.of(bookmarkId))).thenReturn(0L);
            when(bookmarkRepository.findTagIdsByUserIdAndBookmarkIds(USER_ID, List.of(bookmarkId))).thenReturn(Set.of(1L, 2L));

            bookmarkService.restoreFromTrash(USER_ID, bookmarkId);

            verify(bookmarkRepository).restoreDeletedByUserIdAndIdIn(USER_ID, List.of(bookmarkId));
            verify(tagService).onTagsAttached(Set.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("deleteFromTrash")
    class DeleteFromTrashTest {
        @Test
        @DisplayName("휴지통에 없는 북마크를 영구 삭제 시 예외가 발생한다")
        void delete_from_trash_throws_when_bookmark_is_not_deleted() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");

            when(bookmarkRepository.findIncludingDeletedByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));

            assertThatThrownBy(() -> bookmarkService.deleteFromTrash(USER_ID, bookmarkId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE));
        }

        @Test
        @DisplayName("휴지통에서 영구 삭제 시 관련 태그도 정리한다")
        void delete_from_trash_detaches_tags_when_deleted() {
            Long bookmarkId = 100L;
            Bookmark bookmark = bookmark(bookmarkId, user(USER_ID), collectionRef(), "https://example.com");
            bookmark.softDelete(FIXED_INSTANT.minusSeconds(60));
            bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, tag(1L)));
            bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, tag(2L)));

            when(bookmarkRepository.findIncludingDeletedByIdAndUserId(bookmarkId, USER_ID)).thenReturn(Optional.of(bookmark));
            when(bookmarkRepository.deleteDeletedByUserIdAndId(USER_ID, bookmarkId)).thenReturn(1);

            bookmarkService.deleteFromTrash(USER_ID, bookmarkId);

            verify(tagService).onTagsDetached(eq(Set.of(1L, 2L)), any(Instant.class));
        }
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .build();
    }

    private Collection collectionRef() {
        return org.mockito.Mockito.mock(Collection.class);
    }

    private Collection collectionWithId(Long id) {
        Collection collection = collectionRef();
        when(collection.getId()).thenReturn(id);
        return collection;
    }

    private Collection ownedCollection(Long userId) {
        User user = user(userId);
        Collection collection = org.mockito.Mockito.mock(Collection.class);
        when(collection.getUser()).thenReturn(user);
        return collection;
    }

    private Bookmark bookmark(Long id, User user, Collection collection, String url) {
        return Bookmark.builder()
                .id(id)
                .user(user)
                .collection(collection)
                .url(url)
                .title("title")
                .description("desc")
                .emoji("🔖")
                .imageMode(ImageMode.AUTO)
                .bookmarkTags(new HashSet<>())
                .createdAt(FIXED_INSTANT)
                .updatedAt(FIXED_INSTANT)
                .build();
    }

    private BookmarkRes bookmarkRes(
            Long id,
            Long collectionId,
            String url,
            ImageMode imageMode,
            AutoImageStatus autoImageStatus
    ) {
        return new BookmarkRes(
                id,
                collectionId,
                url,
                "title",
                "desc",
                "🔖",
                null,
                null,
                imageMode,
                autoImageStatus,
                false,
                List.of(),
                FIXED_INSTANT,
                FIXED_INSTANT
        );
    }

    private Tag tag(Long id) {
        Tag tag = org.mockito.Mockito.mock(Tag.class);
        when(tag.getId()).thenReturn(id);
        return tag;
    }
}
