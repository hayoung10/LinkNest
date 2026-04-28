-- collections.user_id -> users.id
ALTER TABLE collections
    DROP FOREIGN KEY fk_collection_user;

ALTER TABLE collections
    ADD CONSTRAINT fk_collection_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE;

-- bookmarks.user_id -> users.id
ALTER TABLE bookmarks
    DROP FOREIGN KEY fk_bookmark_user;

ALTER TABLE bookmarks
    ADD CONSTRAINT fk_bookmark_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE;

-- user_preferences.user_id -> users.id
ALTER TABLE user_preferences
    DROP FOREIGN KEY fk_user_preferences_user;

ALTER TABLE user_preferences
    ADD CONSTRAINT fk_user_preferences_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE;

-- bookmarks.collection_id -> collections.id
ALTER TABLE bookmarks
    DROP FOREIGN KEY fk_bookmark_collection;

ALTER TABLE bookmarks
    ADD CONSTRAINT fk_bookmark_collection
        FOREIGN KEY (collection_id) REFERENCES collections (id)
        ON DELETE CASCADE;

-- collections.parent_id -> collections.id
ALTER TABLE collections
    DROP FOREIGN KEY fk_collection_parent;

ALTER TABLE collections
    ADD CONSTRAINT fk_collection_parent
        FOREIGN KEY (parent_id) REFERENCES collections (id)
        ON DELETE CASCADE;