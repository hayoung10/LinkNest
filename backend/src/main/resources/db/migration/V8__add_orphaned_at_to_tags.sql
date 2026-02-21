-- tags 테이블
ALTER TABLE tags
    ADD COLUMN orphaned_at DATETIME(6) NULL AFTER deleted_at;

CREATE INDEX idx_tags_orphaned_at
    ON tags (orphaned_at);