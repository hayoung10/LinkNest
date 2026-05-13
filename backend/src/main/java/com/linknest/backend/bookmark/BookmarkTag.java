package com.linknest.backend.bookmark;

import com.linknest.backend.tag.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity @Table(name = "bookmark_tags",
        indexes = {
                @Index(name = "idx_bookmark_tags_tag_id", columnList = "tag_id")
        }
)
@IdClass(BookmarkTagId.class)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BookmarkTag {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_id", nullable = false)
    private Bookmark bookmark;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static BookmarkTag create(Bookmark bookmark, Tag tag) {
        BookmarkTag bt = new BookmarkTag();
        bt.bookmark = bookmark;
        bt.tag = tag;
        return bt;
    }
}
