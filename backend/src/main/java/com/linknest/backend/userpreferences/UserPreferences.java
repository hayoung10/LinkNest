package com.linknest.backend.userpreferences;

import com.linknest.backend.user.User;
import com.linknest.backend.userpreferences.domain.DefaultBookmarkSort;
import com.linknest.backend.userpreferences.domain.DefaultLayout;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "user_preferences")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPreferences {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_bookmark_sort", nullable = false, length = 20)
    private DefaultBookmarkSort defaultBookmarkSort;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_layout", nullable = false, length = 20)
    private DefaultLayout defaultLayout;

    @Column(name = "open_in_new_tab", nullable = false)
    private boolean openInNewTab;

    @Column(name = "keep_signed_in", nullable = false)
    private boolean keepSignedIn;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
