package com.linknest.backend.tag.dto;

import com.linknest.backend.common.dto.PageMeta;
import org.springframework.data.domain.Page;

import java.util.List;

public record TagsRes(
        List<TagRes> items,
        PageMeta meta,
        long totalBookmarks
) {
    public static TagsRes of(Page<TagRes> page, long totalBookmarks) {
        return new TagsRes(
                page.getContent(),
                new PageMeta(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()),
                totalBookmarks
        );
    }
}
