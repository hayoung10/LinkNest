package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.dto.*;
import com.linknest.backend.common.dto.SliceResponse;
import com.linknest.backend.common.response.ApiResponse;
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

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/bookmarks")
public class BookmarkController {
    private final BookmarkService service;

    @PostMapping
    public ResponseEntity<ApiResponse<BookmarkRes>> create(@AuthenticationPrincipal(expression = "id") Long userId,
                                                          @RequestBody @Valid BookmarkCreateReq req) {
        BookmarkRes data = service.create(userId, req);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(data.id())
                .toUri();

        return ResponseEntity.created(location).body(ApiResponse.ok("북마크 생성", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookmarkRes>> get(@AuthenticationPrincipal(expression = "id") Long userId,
                                           @PathVariable @Min(1) Long id) {
        BookmarkRes data = service.get(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("북마크 조회", data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BookmarkRes>> update(@AuthenticationPrincipal(expression = "id") Long userId,
                                              @PathVariable @Min(1) Long id,
                              @RequestBody @Valid BookmarkUpdateReq req) {
        BookmarkRes data = service.update(userId, id, req);
        return ResponseEntity.ok(ApiResponse.ok("북마크 수정", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal(expression = "id") Long userId,
                                       @PathVariable @Min(1) Long id) {
        service.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("북마크 삭제 완료"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<BookmarkRes>>> listBookmarks(@AuthenticationPrincipal(expression = "id") Long userId,
                                                                                 @RequestParam @Min(1) Long collectionId,
                                                                                 @RequestParam(required = false) String q,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "20") int size) {
        SliceResponse<BookmarkRes> data = service.listByCollection(userId, collectionId, q, page, size);
        return ResponseEntity.ok(ApiResponse.ok("북마크 목록 조회", data));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<ApiResponse<Void>> move(@AuthenticationPrincipal(expression = "id") Long userId,
                                     @PathVariable @Min(1) Long id,
                                     @RequestBody @Valid BookmarkMoveReq req) {
        service.move(userId, id, req.targetCollectionId());
        return ResponseEntity.ok(ApiResponse.ok("북마크 이동 완료"));
    }

    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookmarkRes>> uploadCover(@AuthenticationPrincipal(expression = "id") Long userId,
                                                   @PathVariable @Min(1) Long id,
                                                   @RequestPart("file") MultipartFile coverImage) {
        BookmarkRes data = service.uploadCover(userId, id, coverImage);
        return ResponseEntity.ok(ApiResponse.ok("북마크 커버 업로드", data));
    }

    @DeleteMapping("/{id}/cover")
    public ResponseEntity<ApiResponse<BookmarkRes>> removeCover(@AuthenticationPrincipal(expression = "id") Long userId,
                                                   @PathVariable @Min(1) Long id) {
        BookmarkRes data = service.removeCover(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("북마크 커버 삭제", data));
    }

    @PatchMapping("/{id}/image-mode")
    public ResponseEntity<ApiResponse<BookmarkRes>> updateImageMode(@AuthenticationPrincipal(expression = "id") Long userId,
                                                       @PathVariable @Min(1) Long id,
                                                       @RequestBody @Valid BookmarkImageModeUpdateReq req) {
        BookmarkRes data = service.updateImageMode(userId, id, req.imageMode());
        return ResponseEntity.ok(ApiResponse.ok("북마크 이미지 모드 변경", data));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<BookmarkRes>> updateFavorite(@AuthenticationPrincipal(expression = "id") Long userId,
                                                      @PathVariable @Min(1) Long id,
                                                      @RequestBody @Valid BookmarkFavoriteUpdateReq req) {
        BookmarkRes data = service.updateFavorite(userId, id, req.isFavorite());
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기 변경", data));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<SliceResponse<BookmarkRes>>> listFavorites(@AuthenticationPrincipal(expression = "id") Long userId,
                                                                                 @RequestParam(required = false) String q,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "20") int size) {
        SliceResponse<BookmarkRes> data = service.listByFavorites(userId, q, page, size);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기 목록 조회", data));
    }
}
