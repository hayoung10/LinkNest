package com.linknest.backend.collection;

import com.linknest.backend.collection.dto.request.*;
import com.linknest.backend.collection.dto.response.*;
import com.linknest.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/collections")
public class CollectionController {
    private final CollectionService service;

    @PostMapping
    public ResponseEntity<ApiResponse<CollectionRes>> create(@AuthenticationPrincipal(expression = "id") Long userId,
                                                            @RequestBody @Valid CollectionCreateReq req) {
        CollectionRes data = service.create(userId, req);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(data.id())
                .toUri();

        return ResponseEntity.created(location).body(ApiResponse.ok("컬렉션 생성", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CollectionRes>> get(@AuthenticationPrincipal(expression = "id") Long userId,
                                             @PathVariable @Min(1) Long id) {
        CollectionRes data = service.get(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("컬렉션 조회", data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CollectionRes>> update(@AuthenticationPrincipal(expression = "id") Long userId,
                                                @PathVariable @Min(1) Long id,
                                                @RequestBody @Valid CollectionUpdateReq req) {
        CollectionRes data = service.update(userId, id, req);
        return ResponseEntity.ok(ApiResponse.ok("컬렉션 수정", data));
    }

    @PatchMapping("/{id}/emoji")
    public ResponseEntity<ApiResponse<CollectionRes>> updateEmoji(@AuthenticationPrincipal(expression = "id") Long userId,
                                                     @PathVariable @Min(1) Long id,
                                                     @RequestBody @Valid CollectionEmojiUpdateReq req) {
        CollectionRes data = service.updateEmoji(userId, id, req);
        return ResponseEntity.ok(ApiResponse.ok("컬렉션 이모지 변경", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal(expression = "id") Long userId,
                                       @PathVariable @Min(1) Long id) {
        service.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("컬렉션 삭제 완료"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionRes>>> listChildren(@AuthenticationPrincipal(expression = "id") Long userId,
                                                            @RequestParam(required = false) Long parentId) {
        List<CollectionRes> data = service.listChildren(userId, parentId);
        return ResponseEntity.ok(ApiResponse.ok("컬렉션 목록 조회", data));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<ApiResponse<CollectionPositionRes>> move(@AuthenticationPrincipal(expression = "id") Long userId,
                                                      @PathVariable @Min(1) Long id,
                                                      @RequestBody @Valid CollectionMoveReq req) {
        CollectionPositionRes data = service.move(userId, id, req.targetParentId());
        return ResponseEntity.ok(ApiResponse.ok("컬렉션 이동", data));
    }

    @PatchMapping("/{id}/reorder")
    public ResponseEntity<ApiResponse<CollectionPositionRes>> reorder(@AuthenticationPrincipal(expression = "id") Long userId,
                                        @PathVariable @Min(1) Long id,
                                        @RequestBody @Valid CollectionReorderReq req) {
        CollectionPositionRes data = service.reorder(userId, id, req.targetIndex());
        return ResponseEntity.ok(ApiResponse.ok("컬렉션 순서 변경", data));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CollectionNodeRes>>> tree(@AuthenticationPrincipal(expression = "id") Long userId) {
        List<CollectionNodeRes> data = service.listTree(userId);
        return ResponseEntity.ok(ApiResponse.ok("컬렉션 트리 조회", data));
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<ApiResponse<List<CollectionPathRes>>> path(@AuthenticationPrincipal(expression = "id") Long userId,
                                                        @PathVariable @Min(1) Long id) {
        List<CollectionPathRes> data = service.getPath(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("컬렉션 경로 조회", data));
    }
}
