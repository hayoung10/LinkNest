-- collections 테이블
ALTER TABLE collections
    ADD COLUMN deleted_at DATETIME(6) NULL AFTER updated_at;

CREATE INDEX idx_collections_user_deleted_at
    ON collections (user_id, deleted_at);

-- bookmarks 테이블
ALTER TABLE bookmarks
    ADD COLUMN deleted_at DATETIME(6) NULL AFTER updated_at;

CREATE INDEX idx_bookmarks_user_deleted_at
    ON bookmarks (user_id, deleted_at);

-- tags 테이블
ALTER TABLE tags
    ADD COLUMN deleted_at DATETIME(6) NULL AFTER updated_at;

CREATE INDEX idx_tags_user_deleted_at
    ON tags (user_id, deleted_at);