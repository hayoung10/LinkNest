package com.linknest.backend.tag;

import com.linknest.backend.tag.dto.TagRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByIdAndUserId(Long id, Long userId);

    List<Tag> findByUserIdAndNameKeyIn(Long userId, Collection<String> nameKeys);

    Optional<Tag> findByUserIdAndNameKey(Long userId, String nameKey);

    @Query(value = "select t.id from tags t " +
            "where t.created_at < :cutoff " +
            "and not exists (select 1 from bookmark_tags bt where bt.tag_id = t.id) " +
            "limit :batchSize", nativeQuery = true)
    List<Long> findOrphanTagIds(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);

    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and (:q is null or :q = '' or lower(t.name) like lower(concat('%', :q, '%'))) " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by t.createdAt desc")
    Page<TagRes> findAllByUserIdAndNameLikeOrderByCreatedAtDesc(Long userId, String q, Pageable pageable);

    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and (:q is null or :q = '' or lower(t.name) like lower(concat('%', :q, '%'))) " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by t.createdAt asc")
    Page<TagRes> findAllByUserIdAndNameLikeOrderByCreatedAtAsc(Long userId, String q, Pageable pageable);

    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and (:q is null or :q = '' or lower(t.name) like lower(concat('%', :q, '%'))) " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by t.name asc")
    Page<TagRes> findAllByUserIdAndNameLikeOrderByNameAsc(Long userId, String q, Pageable pageable);

    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and (:q is null or :q = '' or lower(t.name) like lower(concat('%', :q, '%'))) " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by t.name desc")
    Page<TagRes> findAllByUserIdAndNameLikeOrderByNameDesc(Long userId, String q, Pageable pageable);

    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and (:q is null or :q = '' or lower(t.name) like lower(concat('%', :q, '%'))) " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by count(distinct b.id) desc, t.name asc")
    Page<TagRes> findAllByUserIdAndNameLikeOrderByBookmarkCountDesc(Long userId, String q, Pageable pageable);

    @Query("select new com.linknest.backend.tag.dto.TagRes(t.id, t.name, t.createdAt, t.updatedAt, count(distinct b.id)) " +
            "from Tag t " +
            "   left join t.bookmarkTags bt " +
            "   left join bt.bookmark b " +
            "where t.user.id = :userId " +
            "   and (:q is null or :q = '' or lower(t.name) like lower(concat('%', :q, '%'))) " +
            "group by t.id, t.name, t.createdAt, t.updatedAt " +
            "order by count(distinct b.id) asc, t.name asc")
    Page<TagRes> findAllByUserIdAndNameLikeOrderByBookmarkCountAsc(Long userId, String q, Pageable pageable);
}
