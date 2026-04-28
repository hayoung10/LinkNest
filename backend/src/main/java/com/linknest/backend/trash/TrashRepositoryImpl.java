package com.linknest.backend.trash;

import com.linknest.backend.tag.Tag;
import com.linknest.backend.trash.dto.TrashBookmarkRow;
import com.linknest.backend.trash.dto.TrashCollectionRow;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrashRepositoryImpl implements TrashRepository {
    private final EntityManager em;

    @Override
    public List<TrashCollectionRow> findDeletedCollections(Long userId, int offset, int limit) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "select c.id, c.name, c.emoji, " +
                        "   p.name as parent_name, p.emoji as parent_emoji, c.deleted_at " +
                        "from collections c " +
                        "left join collections p on p.id = c.parent_id " +
                        "where c.user_id = :userId and c.deleted_at is not null " +
                        "order by c.deleted_at desc, c.id desc " +
                        "limit :limit offset :offset"
                )
                .setParameter("userId", userId)
                .setParameter("limit", limit)
                .setParameter("offset", offset)
                .getResultList();

        return rows.stream().map(this::mapCollectionRow).toList();
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
                "select b.id, b.title, b.url, b.emoji, " +
                        "   c.name as collection_name, c.emoji as collection_emoji, b.deleted_at " +
                        "from bookmarks b " +
                        "left join collections c on c.id = b.collection_id " +
                        "where b.user_id = :userId and b.deleted_at is not null " +
                        "order by b.deleted_at desc, b.id desc " +
                        "limit :limit offset :offset"
                )
                .setParameter("userId", userId)
                .setParameter("limit", limit)
                .setParameter("offset", offset)
                .getResultList();

        return rows.stream().map(this::mapBookmarkRow).toList();
    }

    private TrashCollectionRow mapCollectionRow(Object[] rows) {
        Long id = ((Number) rows[0]).longValue();
        String name = (String) rows[1];
        String emoji = (String) rows[2];

        String parentName = (String) rows[3];
        String parentEmoji = (String) rows[4];

        Instant deletedAt = null;
        Object ts = rows[5];
        if(ts instanceof Timestamp t) deletedAt = t.toInstant();
        else if(ts instanceof java.util.Date d) deletedAt = d.toInstant();
        else if(ts instanceof Instant i) deletedAt = i;

        return new TrashCollectionRow(id, name, emoji, parentName, parentEmoji, deletedAt);
    }

    private TrashBookmarkRow mapBookmarkRow(Object[] rows) {
        Long id = ((Number) rows[0]).longValue();
        String title = (String) rows[1];
        String url = (String) rows[2];
        String emoji = (String) rows[3];

        String collectionName = (String) rows[4];
        String collectionEmoji = (String) rows[5];

        Instant deletedAt = null;
        Object ts = rows[6];
        if(ts instanceof Timestamp t) deletedAt = t.toInstant();
        else if(ts instanceof java.util.Date d) deletedAt = d.toInstant();
        else if(ts instanceof Instant i) deletedAt = i;

        return new TrashBookmarkRow(id, title, url, emoji, collectionName, collectionEmoji, deletedAt);
    }

    @Override
    public Map<Long, Long> countDeletedChildCollections(Long userId, List<Long> parentIds) {
        if(parentIds == null || parentIds.isEmpty()) return Map.of();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "select c.parent_id, count(*) from collections c " +
                        "where c.user_id = :userId " +
                        "   and c.deleted_at is not null " +
                        "   and c.parent_id in (:ids) " +
                        "group by c.parent_id"
                )
                .setParameter("userId", userId)
                .setParameter("ids", parentIds)
                .getResultList();

        Map<Long, Long> map = new HashMap<>();
        for(Object[] r : rows) {
            Long parentId = ((Number) r[0]).longValue();
            Long cnt = ((Number) r[1]).longValue();
            map.put(parentId, cnt);
        }
        return map;
    }

    @Override
    public Map<Long, Long> countDeletedBookmarksInCollections(Long userId, List<Long> collectionIds) {
        if(collectionIds == null || collectionIds.isEmpty()) return Map.of();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        "select b.collection_id, count(*) from bookmarks b " +
                                "where b.user_id = :userId " +
                                "   and b.deleted_at is not null " +
                                "   and b.collection_id in (:ids) " +
                                "group by b.collection_id"
                )
                .setParameter("userId", userId)
                .setParameter("ids", collectionIds)
                .getResultList();

        Map<Long, Long> map = new HashMap<>();
        for(Object[] r : rows) {
            Long collectionId = ((Number) r[0]).longValue();
            Long cnt = ((Number) r[1]).longValue();
            map.put(collectionId, cnt);
        }
        return map;
    }

    @Override
    public Map<Long, Long> countTaggedCountByTagIds(Long userId, List<Long> tagIds) {
        if(tagIds == null || tagIds.isEmpty()) return Map.of();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        "select bt.tag_id, count(*) from bookmark_tags bt " +
                                "   join bookmarks b on b.id = bt.bookmark_id " +
                                "where b.user_id = :userId " +
                                "   and bt.tag_id in (:ids) " +
                                "group by bt.tag_id"
                )
                .setParameter("userId", userId)
                .setParameter("ids", tagIds)
                .getResultList();

        Map<Long, Long> map = new HashMap<>();
        for(Object[] r : rows) {
            Long tagId = ((Number) r[0]).longValue();
            Long cnt = ((Number) r[1]).longValue();
            map.put(tagId, cnt);
        }
        return map;
    }
}
