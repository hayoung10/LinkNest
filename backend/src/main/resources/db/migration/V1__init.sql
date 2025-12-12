-- 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    profile_image_url VARCHAR(512),
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_provider_provider_id UNIQUE (provider, provider_id),
    INDEX idx_provider_pid (provider, provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 컬렉션 테이블
CREATE TABLE collections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    icon VARCHAR(255),
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    sort_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_collection_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_collection_parent
        FOREIGN KEY (parent_id) REFERENCES collections (id),
    INDEX idx_collection_parent (parent_id),
    INDEX idx_collection_user_parent (user_id, parent_id),
    INDEX idx_collection_user_parent_order (user_id, parent_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 북마크 테이블
CREATE TABLE bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(2048) NOT NULL,
    title VARCHAR(255),
    description VARCHAR(1000),
    user_id BIGINT NOT NULL,
    collection_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_bookmark_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookmark_collection
        FOREIGN KEY (collection_id) REFERENCES collections (id),
    INDEX idx_bookmark_user (user_id),
    INDEX idx_bookmark_collection (collection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 사용자 환경설정 테이블
CREATE TABLE user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    default_bookmark_sort VARCHAR(20) NOT NULL,
    default_layout VARCHAR(20) NOT NULL,
    open_in_new_tab TINYINT(1) NOT NULL,
    keep_signed_in TINYINT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_user_preferences_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_user_preferences_user UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;