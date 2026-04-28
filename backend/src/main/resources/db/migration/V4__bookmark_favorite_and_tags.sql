-- bookmarks에 즐겨찾기 컬럼 추가
ALTER TABLE bookmarks
    ADD COLUMN is_favorite TINYINT(1) NOT NULL DEFAULT 0;

-- tags 테이블
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_tags_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_tags_user_name UNIQUE (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- bookmark_tags (M:N 조인 테이블)
CREATE TABLE bookmark_tags (
    bookmark_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (bookmark_id, tag_id),
    CONSTRAINT fk_bookmark_tags_bookmark
        FOREIGN KEY (bookmark_id) REFERENCES bookmarks(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bookmark_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_bookmark_tags_tag_id ON bookmark_tags (tag_id);
CREATE INDEX idx_tags_user ON tags (user_id);