package com.linknest.backend.tag;

import com.linknest.backend.tag.dto.TagRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByIdAndUserId(Long id, Long userId);

    List<Tag> findByUserIdAndNameKeyIn(Long userId, Collection<String> nameKeys);

    Optional<Tag> findByUserIdAndNameKey(Long userId, String nameKey);

    @Query(value = "select * from tags " +
            "where user_id = :userId and name_key = :nameKey", nativeQuery = true)
    Optional<Tag> findIncludingDeletedByUserIdAndNameKey(@Param("userId") Long userId, String nameKey);

    @Query(value = "select * from tags " +
            "where user_id = :userId and name_key in (:nameKeys)", nativeQuery = true)
    List<Tag> findAllIncludingDeletedByUserIdAndNameKeyIn(@Param("userId") Long userId,
                                                          @Param("nameKeys") Collection<String> nameKeys);

    @Query(value = "select * from tags " +
            "where id = :id and user_id = :userId", nativeQuery = true)
    Optional<Tag> findIncludingDeletedByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // -------------------- Tag Cleanup --------------------
    @Query(value = "select t.id from tags t " +
            "where t.deleted_at is null " +
            "and t.created_at < :cutoff " +
            "and not exists (select 1 from bookmark_tags bt where bt.tag_id = t.id) " +
            "limit :batchSize", nativeQuery = true)
    List<Long> findOrphanTagIds(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);

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

    // -------------------- Hard Delete --------------------
    @Modifying
    @Query(value = "delete from tags where id = :id and user_id = :userId", nativeQuery = true)
    int hardDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
