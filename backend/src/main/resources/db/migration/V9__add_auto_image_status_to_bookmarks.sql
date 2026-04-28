ALTER TABLE bookmarks
    ADD COLUMN auto_image_status VARCHAR(20) NULL
    AFTER auto_image_url;