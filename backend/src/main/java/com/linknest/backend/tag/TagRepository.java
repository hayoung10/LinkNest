package com.linknest.backend.tag;

import com.linknest.backend.tag.dto.TagRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByIdAndUserId(Long id, Long userId);

    Optional<Tag> findByUserIdAndNameKey(Long userId, String nameKey);

    @Query(value = "select * from tags " +
            "where user_id = :userId and name_key = :nameKey", nativeQuery = true)
    Optional<Tag> findIncludingDeletedByUserIdAndNameKey(@Param("userId") Long userId, String nameKey);

    @Query(value = "select * from tags " +
            "where user_id = :userId and name_key in (:nameKeys)", nativeQuery = true)
    List<Tag> findAllIncludingDeletedByUserIdAndNameKeyIn(@Param("userId") Long userId,
                                                          @Param("nameKeys") Collection<String> nameKeys);

    @Query(value = "select * from tags where id = :id and user_id = :userId", nativeQuery = true)
    Optional<Tag> findIncludingDeletedByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // -------------------- Unused Tag --------------------
    @Query(value = "select t.id from tags t " +
            "where t.deleted_at is null " +
            "   and t.orphaned_at is not null " +
            "   and t.orphaned_at < :cutoff " +
            "limit :batchSize", nativeQuery = true)
    List<Long> findOrphanTagIds(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);

    @Modifying
    @Query(value = "update tags set deleted_at = :now " +
            "where id in (:ids) and deleted_at is null", nativeQuery = true)
    int moveToTrashByIds(@Param("ids") List<Long> ids, @Param("now") Instant now);

    @Modifying
    @Query(value = "update tags set orphaned_at = null " +
            "where id in (:ids) and deleted_at is null and orphaned_at is not null", nativeQuery = true)
    int clearOrphanedAtByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "update tags t set orphaned_at = :now " +
            "where id in (:ids) " +
            "   and deleted_at is null " +
            "   and not exists (" +
            "       select 1 from bookmark_tags bt " +
            "           join bookmarks b on b.id = bt.bookmark_id " +
            "       where bt.tag_id = t.id and b.deleted_at is null" +
            ")", nativeQuery = true)
    int setOrphanedAtIfUnusedByIds(@Param("ids") List<Long> ids, @Param("now") Instant now);

    @Modifying
    @Query(value = "update tags set orphaned_at = :now " +
            "where id in (:ids) and deleted_at is null", nativeQuery = true)
    int resetOrphanedAtByIds(@Param("ids") List<Long> ids, @Param("now") Instant now);

    // -------------------- Tagged Bookmarks --------------------
    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and t.deletedAt is null " +
            "   and (b is null or b.deletedAt is null) " +
            "   and (:pattern is null or lower(t.name) like :pattern escape '\\') " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by t.createdAt desc")
    Slice<TagRes> findAllByUserIdAndNameLikeOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("pattern") String pattern,
                                                                 Pageable pageable);

    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and t.deletedAt is null " +
            "   and (b is null or b.deletedAt is null) " +
            "   and (:pattern is null or lower(t.name) like :pattern escape '\\') " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by t.createdAt asc")
    Slice<TagRes> findAllByUserIdAndNameLikeOrderByCreatedAtAsc(@Param("userId") Long userId, @Param("pattern") String pattern,
                                                                Pageable pageable);

    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and t.deletedAt is null " +
            "   and (b is null or b.deletedAt is null) " +
            "   and (:pattern is null or lower(t.name) like :pattern escape '\\') " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by t.name asc")
    Slice<TagRes> findAllByUserIdAndNameLikeOrderByNameAsc(@Param("userId") Long userId, @Param("pattern") String pattern,
                                                           Pageable pageable);

    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and t.deletedAt is null " +
            "   and (b is null or b.deletedAt is null) " +
            "   and (:pattern is null or lower(t.name) like :pattern escape '\\') " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by count(distinct b.id) desc, t.name asc, t.id asc")
    Slice<TagRes> findAllByUserIdAndNameLikeOrderByBookmarkCountDesc(@Param("userId") Long userId, @Param("pattern") String pattern,
                                                                     Pageable pageable);

    // -------------------- Summary --------------------
    long countByUserIdAndDeletedAtIsNull(Long userId);

    // -------------------- Trash --------------------
    @Query(value = "select id from tags where user_id = :userId and deleted_at is not null", nativeQuery = true)
    List<Long> findAllDeletedIdsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "update tags set deleted_at = null " +
            "where user_id = :userId " +
            "   and deleted_at is not null " +
            "   and id in (:ids)", nativeQuery = true)
    int restoreDeletedByUserIdAndIdIn(Long userId, List<Long> ids);

    @Modifying
    @Query(value = "delete from tags " +
            "where user_id = :userId " +
            "   and deleted_at is not null " +
            "   and id in (:ids)", nativeQuery = true)
    int deleteDeletedByUserIdAndIdIn(Long userId, List<Long> ids);

    @Query(value = "select distinct t.name_key from tags t " +
            "where t.user_id = :userId " +
            "   and t.deleted_at is not null " +
            "   and t.id in (:ids)", nativeQuery = true)
    List<String> findDeletedNameKeysByUserIdAndIdIn(Long userId, List<Long> ids);

    @Query(value = "select exists(" +
            "   select 1 from tags t " +
            "   where t.user_id = :userId " +
            "       and t.deleted_at is null " +
            "       and t.name_key in (:keys)" +
            ")", nativeQuery = true)
    boolean existsByUserIdAndDeletedAtIsNullAndNameKeyIn(Long userId, List<String> keys);

    @Query(value = "select t.id from tags t " +
            "where t.user_id = :userId " +
            "   and t.deleted_at is not null " +
            "   and t.id in (:ids)", nativeQuery = true)
    List<Long> findDeletedIdsByUserIdAndIdIn(Long userId, List<Long> ids);
}
