package com.linknest.backend.user;

import com.linknest.backend.bookmark.Bookmark;
import com.linknest.backend.collection.Collection;
import com.linknest.backend.user.domain.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = { "provider", "provider_id" }),
        indexes = @Index(name = "idx_provider_pid", columnList = "provider, provider_id")
)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(length = 512)
    private String profileImageUrl; // 사용자 업로드 이미지

    @Column(length = 512)
    private String providerProfileImageUrl; // OAuth 기본 이미지

    // OAuth2 필드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // ROLE_USER, ROLE_ADMIN

    @OneToMany(mappedBy = "user")
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Collection> collections = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public static User oauthSignup(String email, String name, String providerProfileImageUrl,
                                   AuthProvider provider, String providerId) {
        return User.builder()
                .email(email)
                .name(name)
                .providerProfileImageUrl(providerProfileImageUrl)
                .provider(provider)
                .providerId(providerId)
                .role(Role.ROLE_USER)
                .build();
    }

    public String getResolvedProfileImageUrl() {
        if(hasCustomProfileImage()) return profileImageUrl;
        if(providerProfileImageUrl != null && !providerProfileImageUrl.isBlank()) {
            return providerProfileImageUrl;
        }
        return null;
    }

    public boolean hasCustomProfileImage() {
        return profileImageUrl != null && !profileImageUrl.isBlank();
    }

    public void addBookmark(Bookmark b) {
        bookmarks.add(b);
        b.setUser(this);
    }

    public void addCollection(Collection c) {
        collections.add(c);
        c.setUser(this);
    }

    public enum Role { ROLE_USER, ROLE_ADMIN }
}
