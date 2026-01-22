-- name_key 추가
ALTER TABLE tags
    ADD COLUMN name_key VARCHAR(50) NOT NULL AFTER name;

-- 기존 데이터 정규화해서 name_key 채우기
UPDATE tags
    SET name_key = LOWER(TRIM(name));

-- 기존 유니크 제약 제거
ALTER TABLE tags
    DROP INDEX uk_tags_user_name;

-- 새 유니크 제약 추가
ALTER TABLE tags
    ADD CONSTRAINT uk_tags_user_name_key
    UNIQUE (user_id, name_key);

-- 인덱스 추가
CREATE INDEX idx_tags_user_name_key
    ON tags (user_id, name_key);