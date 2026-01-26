package com.linknest.backend.tag;

import com.linknest.backend.common.dto.PageResponse;
import com.linknest.backend.common.response.ApiResponse;
import com.linknest.backend.tag.domain.TagSort;
import com.linknest.backend.tag.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/tags")
public class TagController {
    private final TagService service;

    @PostMapping
    public ResponseEntity<ApiResponse<TagRes>> create(@AuthenticationPrincipal(expression = "id") Long userId,
                                                      @RequestBody @Valid TagCreateReq req) {
        TagRes data = service.create(userId, req);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(data.id())
                .toUri();

        return ResponseEntity.created(location).body(ApiResponse.ok("태그 생성 완료", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<TagsRes>> getTags(@AuthenticationPrincipal(expression = "id") Long userId,
                                                        @RequestParam(required = false) String q,
                                                        @RequestParam(defaultValue = "NAME_ASC") TagSort sort,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        TagsRes data = service.getTags(userId, q, sort, page, size);
        return ResponseEntity.ok(ApiResponse.ok("태그 목록 조회", data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TagRes>> rename(@AuthenticationPrincipal(expression = "id") Long userId,
                                                      @PathVariable @Min(1) Long id,
                                                      @RequestBody @Valid TagUpdateReq req) {
        TagRes data = service.rename(userId, id, req);
        return ResponseEntity.ok(ApiResponse.ok("태그 이름 변경", data));
    }

    @PostMapping("/{id}/merge")
    public ResponseEntity<ApiResponse<Void>> merge(@AuthenticationPrincipal(expression = "id") Long userId,
                                                   @PathVariable @Min(1) Long id,
                                                   @RequestBody @Valid TagMergeReq req) {
        service.merge(userId, id, req);
        return ResponseEntity.ok(ApiResponse.ok("태그 병합 완료", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal(expression = "id") Long userId,
                                                    @PathVariable @Min(1) Long id) {
        service.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("태그 삭제 완료",null));
    }

    // =============================
    // Tagged Bookmarks
    // =============================
    @GetMapping("/{id}/bookmarks")
    public ResponseEntity<ApiResponse<PageResponse>> getTaggedBookmarks(@AuthenticationPrincipal(expression = "id") Long userId,
                                                                        @PathVariable @Min(1) Long id,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size) {
        PageResponse<TaggedBookmarkRes> data = service.getTaggedBookmarks(userId, id, page, size);
        return ResponseEntity.ok(ApiResponse.ok("태그 북마크 목록 조회", data));
    }

    @PostMapping("/{id}/detach")
    public ResponseEntity<ApiResponse<Void>> detachTagFromBookmarks(@AuthenticationPrincipal(expression = "id") Long userId,
                                                                    @PathVariable @Min(1) Long id,
                                                                    @RequestBody @Valid TagDetachReq req) {
        service.detachTagFromBookmarks(userId, id, req);
        return ResponseEntity.ok(ApiResponse.ok("태그 제거 완료", null));
    }

    @PostMapping("/{id}/replace")
    public ResponseEntity<ApiResponse<Void>> replaceTagOnBookmarks(@AuthenticationPrincipal(expression = "id") Long userId,
                                                                   @PathVariable @Min(1) Long id,
                                                                   @RequestBody @Valid TagReplaceReq req) {
        service.replaceTagOnBookmarks(userId, id, req);
        return ResponseEntity.ok(ApiResponse.ok("태그 교체 완료", null));
    }
}
