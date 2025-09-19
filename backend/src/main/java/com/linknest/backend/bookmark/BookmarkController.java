package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.dto.BookmarkCreateReq;
import com.linknest.backend.bookmark.dto.BookmarkMoveReq;
import com.linknest.backend.bookmark.dto.BookmarkRes;
import com.linknest.backend.bookmark.dto.BookmarkUpdateReq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<BookmarkRes> create(@RequestParam Long userId,
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
    public ResponseEntity<BookmarkRes> get(@RequestParam Long userId,
                                           @PathVariable @Min(1) Long id) {
        BookmarkRes res = service.get(userId, id);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookmarkRes> update(@RequestParam Long userId,
                                              @PathVariable @Min(1) Long id,
                              @RequestBody @Valid BookmarkUpdateReq req) {
        BookmarkRes res = service.update(userId, id, req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam Long userId,
                                       @PathVariable @Min(1) Long id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BookmarkRes>> listBookmarks(@RequestParam Long userId,
                                                           @RequestParam(required = false) Long collectionId) {
        List<BookmarkRes> res = service.listByCollection(userId, collectionId);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<Void> move(@RequestParam Long userId,
                                     @PathVariable @Min(1) Long id,
                                     @RequestBody @Valid BookmarkMoveReq req) {
        service.move(userId, id, req.targetCollectionId());
        return ResponseEntity.noContent().build();
    }
}
