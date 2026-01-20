package com.linknest.backend.tag;

import com.linknest.backend.common.dto.PageResponse;
import com.linknest.backend.common.response.ApiResponse;
import com.linknest.backend.tag.domain.TagSort;
import com.linknest.backend.tag.dto.TagMergeReq;
import com.linknest.backend.tag.dto.TagUpdateReq;
import com.linknest.backend.tag.dto.TagRes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/tags")
public class TagController {
    private final TagService service;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TagRes>>> getTags(@AuthenticationPrincipal(expression = "id") Long userId,
                                                                     @RequestParam(required = false) String q,
                                                                     @RequestParam(defaultValue = "NAME_ASC") TagSort sort,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        PageResponse<TagRes> data = service.getTags(userId, q, sort, page, size);
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
}
