package com.linknest.backend.bookmark;

import java.io.Serializable;
import java.util.Objects;

public class BookmarkTagId implements Serializable {
    private Long bookmark;
    private Long tag;

    public BookmarkTagId(){}

    public BookmarkTagId(Long bookmark, Long tag) {
        this.bookmark = bookmark;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        BookmarkTagId bookmarkTagId = (BookmarkTagId) o;
        return Objects.equals(bookmark, bookmarkTagId.bookmark)
                && Objects.equals(tag, bookmarkTagId.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookmark, tag);
    }
}
