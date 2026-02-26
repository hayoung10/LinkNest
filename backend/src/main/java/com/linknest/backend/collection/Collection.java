package com.linknest.backend.collection;

import com.linknest.backend.bookmark.Bookmark;
import com.linknest.backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "collections",
        indexes = {
                @Index(name = "idx_collection_parent", columnList = "parent_id"),
                @Index(name = "idx_collection_user_parent", columnList = "user_id, parent_id"),
                @Index(name = "idx_collection_user_parent_order", columnList = "user_id, parent_id, sort_order"),
                @Index(name = "idx_collections_user_deleted_at", columnList = "user_id, deleted_at")
        }
)
@SQLDelete(sql = "UPDATE collections SET deleted_at = NOW(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Collection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 255)
    private String name;

    @Column(length = 16)
    private String emoji;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Collection parent;

    @OneToMany(mappedBy = "parent")
    private List<Collection> children = new ArrayList<>();

    @OneToMany(mappedBy = "collection")
    private List<Bookmark> bookmarks = new ArrayList<>();

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

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

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
