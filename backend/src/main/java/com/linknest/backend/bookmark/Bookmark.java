package com.linknest.backend.bookmark;

import com.linknest.backend.collection.Collection;
import com.linknest.backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "bookmarks",
        indexes = {
                @Index(name = "idx_bookmark_user", columnList = "user_id"),
                @Index(name = "idx_bookmark_collection", columnList = "collection_id"),
                @Index(name = "idx_bookmarks_user_deleted_at", columnList = "user_id, deleted_at")
        })
@SQLDelete(sql = "UPDATE bookmarks SET deleted_at = NOW(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bookmark {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 2048)
    private String url;

    @Column(length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Column(length = 16)
    private String emoji;

    @Column(name = "auto_image_url", length = 1000)
    private String autoImageUrl;

    @Column(name = "custom_image_url", length = 1000)
    private String customImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_mode", length = 10, nullable = false)
    private ImageMode imageMode = ImageMode.AUTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "auto_image_status", length = 20)
    private AutoImageStatus autoImageStatus;

    @Column(name = "is_favorite", nullable = false)
    private boolean isFavorite = false;

    @OneToMany(mappedBy = "bookmark", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Builder.Default
    private Set<BookmarkTag> bookmarkTags = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public void softDelete(Instant now) {
        this.deletedAt = now;
    }

    public void restore() {
        this.deletedAt = null;
    }

    public enum ImageMode {
        AUTO, CUSTOM, NONE
    }

    public enum AutoImageStatus {
        PENDING, SUCCESS, FAILED
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
