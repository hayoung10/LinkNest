package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/bookmarks")
public class BookmarkController {
    private final BookmarkService service;

    @PostMapping
    public ResponseEntity<BookmarkRes> create(@AuthenticationPrincipal(expression = "id") Long userId,
                                              @RequestBody @Valid BookmarkCreateReq req) {
        BookmarkRes res = service.create(userId, req);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(res.id())
                .toUri();

        return ResponseEntity.created(location).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookmarkRes> get(@AuthenticationPrincipal(expression = "id") Long userId,
                                           @PathVariable @Min(1) Long id) {
        BookmarkRes res = service.get(userId, id);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookmarkRes> update(@AuthenticationPrincipal(expression = "id") Long userId,
                                              @PathVariable @Min(1) Long id,
                              @RequestBody @Valid BookmarkUpdateReq req) {
        BookmarkRes res = service.update(userId, id, req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal(expression = "id") Long userId,
                                       @PathVariable @Min(1) Long id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BookmarkRes>> listBookmarks(@AuthenticationPrincipal(expression = "id") Long userId,
                                                           @RequestParam @Min(1) Long collectionId) {
        List<BookmarkRes> res = service.listByCollection(userId, collectionId);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<Void> move(@AuthenticationPrincipal(expression = "id") Long userId,
                                     @PathVariable @Min(1) Long id,
                                     @RequestBody @Valid BookmarkMoveReq req) {
        service.move(userId, id, req.targetCollectionId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookmarkRes> uploadCover(@AuthenticationPrincipal(expression = "id") Long userId,
                                                   @PathVariable @Min(1) Long id,
                                                   @RequestPart("file") MultipartFile coverImage) {
        BookmarkRes res = service.uploadCover(userId, id, coverImage);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}/cover")
    public ResponseEntity<BookmarkRes> removeCover(@AuthenticationPrincipal(expression = "id") Long userId,
                                                   @PathVariable @Min(1) Long id) {
        BookmarkRes res = service.removeCover(userId, id);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/image-mode")
    public ResponseEntity<BookmarkRes> updateImageMode(@AuthenticationPrincipal(expression = "id") Long userId,
                                                       @PathVariable @Min(1) Long id,
                                                       @RequestBody @Valid BookmarkImageModeUpdateReq req) {
        BookmarkRes res = service.updateImageMode(userId, id, req.imageMode());
        return ResponseEntity.ok(res);
    }
}
