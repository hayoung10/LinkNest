package com.linknest.backend.collection;

import com.linknest.backend.collection.dto.CollectionCreateReq;
import com.linknest.backend.collection.dto.CollectionRes;
import com.linknest.backend.collection.dto.CollectionUpdateReq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/collections")
public class CollectionController {
    private final CollectionService service;

    @PostMapping
    public ResponseEntity<CollectionRes> create(@RequestBody @Valid CollectionCreateReq req) {
        CollectionRes res = service.create(req);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(res.id())
                .toUri();

        return ResponseEntity.created(location).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionRes> get(@PathVariable @Min(1) Long id) {
        CollectionRes res = service.get(id);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("{/id}")
    public ResponseEntity<CollectionRes> update(@PathVariable @Min(1) Long id,
                                                @RequestBody @Valid CollectionUpdateReq req) {
        CollectionRes res = service.update(id, req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
