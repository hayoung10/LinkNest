package com.linknest.backend.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserIdAndNameIn(Long userId, Collection<String> names);

    @Query(value = "select t.id from tags t " +
            "left join bookmark_tags bt on bt.tag_id = t.id " +
            "where bt.tag_id is null and t.created_at < :cutoff " +
            "limit :batchSize", nativeQuery = true)
    List<Long> findOrphanTagIds(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);
}
