package com.linknest.backend.trash;

import com.linknest.backend.collection.Collection;
import com.linknest.backend.tag.Tag;
import com.linknest.backend.trash.dto.TrashBookmarkRow;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrashRepositoryImpl implements TrashRepository {
    private final EntityManager em;

    @Override
    public List<Collection> findDeletedCollections(Long userId, int offset, int limit) {
        @SuppressWarnings("unchecked")
        List<Collection> rows = em.createNativeQuery(
                "select c.* from collections c " +
                        "where c.user_id = :userId and c.deleted_at is not null " +
                        "order by c.deleted_at desc, c.id desc " +
                        "limit :limit offset :offset"
                , Collection.class)
                .setParameter("userId", userId)
                .setParameter("limit", limit)
                .setParameter("offset", offset)
                .getResultList();

        return rows;
    }

    @Override
    public List<Tag> findDeletedTags(Long userId, int offset, int limit) {
        @SuppressWarnings("unchecked")
        List<Tag> rows = em.createNativeQuery(
                        "select t.* from tags t " +
                                "where t.user_id = :userId and t.deleted_at is not null " +
                                "order by t.deleted_at desc, t.id desc " +
                                "limit :limit offset :offset"
                        , Tag.class)
                .setParameter("userId", userId)
                .setParameter("limit", limit)
                .setParameter("offset", offset)
                .getResultList();

        return rows;
    }

    @Override
    public List<TrashBookmarkRow> findDeletedBookmarks(Long userId, int offset, int limit) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "select b.id, b.title, b.url, b.emoji, b.deleted_at " +
                        "c.id as collection_id, " +
                        "c.name as collection_name, " +
                        "c.emoji as collection_emoji " +
                        "from bookmarks b " +
                        "join collections c on c.id = b.collection_id" +
                        "where b.user_id = :userId and b.deleted_at is not null " +
                        "order by b.deleted_at desc, b.id desc " +
                        "limit :limit offset :offset"
                )
                .setParameter("userId", userId)
                .setParameter("limit", limit)
                .setParameter("offset", offset)
                .getResultList();

        return rows.stream().map(this::mapRow).toList();
    }

    private TrashBookmarkRow mapRow(Object[] rows) {
        Long id = ((Number) rows[0]).longValue();
        String title = (String) rows[1];
        String url = (String) rows[2];
        String emoji = (String) rows[3];

        Instant deletedAt = null;
        Object ts = rows[4];
        if(ts instanceof Timestamp t) deletedAt = t.toInstant();
        else if(ts instanceof java.util.Date d) deletedAt = d.toInstant();
        else if(ts instanceof Instant i) deletedAt = i;

        Long collectionId = ((Number) rows[5]).longValue();
        String collectionName = (String) rows[6];
        String collectionEmoji = (String) rows[7];

        return new TrashBookmarkRow(
                id, title, url, emoji, deletedAt,
                collectionId, collectionName, collectionEmoji
        );
    }
}
