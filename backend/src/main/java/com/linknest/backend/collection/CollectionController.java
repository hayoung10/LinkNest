package com.linknest.backend.collection;

import com.linknest.backend.collection.dto.*;
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
    public ResponseEntity<CollectionRes> create(@AuthenticationPrincipal(expression = "id") Long userId,
                                                @RequestBody @Valid CollectionCreateReq req) {
        CollectionRes res = service.create(userId, req);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(res.id())
                .toUri();

        return ResponseEntity.created(location).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionRes> get(@AuthenticationPrincipal(expression = "id") Long userId,
                                             @PathVariable @Min(1) Long id) {
        CollectionRes res = service.get(userId, id);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CollectionRes> update(@AuthenticationPrincipal(expression = "id") Long userId,
                                                @PathVariable @Min(1) Long id,
                                                @RequestBody @Valid CollectionUpdateReq req) {
        CollectionRes res = service.update(userId, id, req);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/emoji")
    public ResponseEntity<CollectionRes> updateEmoji(@AuthenticationPrincipal(expression = "id") Long userId,
                                                     @PathVariable @Min(1) Long id,
                                                     @RequestBody @Valid CollectionEmojiUpdateReq req) {
        CollectionRes res = service.updateEmoji(userId, id, req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal(expression = "id") Long userId,
                                       @PathVariable @Min(1) Long id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CollectionRes>> listChildren(@AuthenticationPrincipal(expression = "id") Long userId,
                                                            @RequestParam(required = false) Long parentId) {
        List<CollectionRes> res = service.listChildren(userId, parentId);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<CollectionMoveRes> move(@AuthenticationPrincipal(expression = "id") Long userId,
                                     @PathVariable @Min(1) Long id,
                                     @RequestBody @Valid CollectionMoveReq req) {
        CollectionMoveRes res = service.move(userId, id, req.targetParentId());
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/order")
    public ResponseEntity<Void> reorder(@AuthenticationPrincipal(expression = "id") Long userId,
                                        @PathVariable @Min(1) Long id,
                                        @RequestBody @Valid CollectionReorderReq req) {
        service.reorder(userId, id, req.newOrder());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CollectionNodeRes>> tree(@AuthenticationPrincipal(expression = "id") Long userId) {
        List<CollectionNodeRes> res = service.listTree(userId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<List<CollectionPathRes>> path(@AuthenticationPrincipal(expression = "id") Long userId,
                                                        @PathVariable @Min(1) Long id) {
        List<CollectionPathRes> res = service.getPath(userId, id);
        return ResponseEntity.ok(res);
    }
}
