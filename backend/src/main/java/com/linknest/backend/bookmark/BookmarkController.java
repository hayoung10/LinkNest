package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.dto.BookmarkCreateReq;
import com.linknest.backend.bookmark.dto.BookmarkRes;
import com.linknest.backend.bookmark.dto.BookmarkUpdateReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {
    private final BookmarkService service;

    @PostMapping
    public BookmarkRes create(@RequestBody @Valid BookmarkCreateReq req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public BookmarkRes get(@PathVariable Long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}")
    public BookmarkRes update(@PathVariable Long id,
                              @RequestBody @Valid BookmarkUpdateReq req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
