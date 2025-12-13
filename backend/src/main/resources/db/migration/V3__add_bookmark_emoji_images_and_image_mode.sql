ALTER TABLE bookmarks
    ADD COLUMN emoji VARCHAR(16),
    ADD COLUMN auto_image_url VARCHAR(1000),
    ADD COLUMN custom_image_url VARCHAR(1000),
    ADD COLUMN image_mode VARCHAR(10) NOT NULL DEFAULT 'AUTO';