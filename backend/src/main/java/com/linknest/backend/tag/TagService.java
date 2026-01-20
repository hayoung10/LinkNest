package com.linknest.backend.tag;

import com.linknest.backend.bookmark.BookmarkTagRepository;
import com.linknest.backend.common.dto.PageResponse;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.tag.domain.TagSort;
import com.linknest.backend.tag.dto.TagMergeReq;
import com.linknest.backend.tag.dto.TagUpdateReq;
import com.linknest.backend.tag.dto.TagRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tagRepository;
    private final BookmarkTagRepository bookmarkTagRepository;
    private final TagMapper tagMapper;

    public PageResponse<TagRes> getTags(Long userId, String q, TagSort sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<TagRes> result = switch(sort) {
            case NEWEST -> tagRepository.findAllByUserIdAndNameLikeOrderByCreatedAtDesc(userId, q, pageable);
            case OLDEST -> tagRepository.findAllByUserIdAndNameLikeOrderByCreatedAtAsc(userId, q, pageable);
            case NAME_ASC -> tagRepository.findAllByUserIdAndNameLikeOrderByNameAsc(userId, q, pageable);
            case NAME_DESC -> tagRepository.findAllByUserIdAndNameLikeOrderByNameDesc(userId, q, pageable);
            case COUNT_DESC -> tagRepository.findAllByUserIdAndNameLikeOrderByBookmarkCountDesc(userId, q, pageable);
            case COUNT_ASC -> tagRepository.findAllByUserIdAndNameLikeOrderByBookmarkCountAsc(userId, q, pageable);
        };

        return PageResponse.of(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public TagRes rename(Long userId, Long id, TagUpdateReq req) {
        Tag tag = requireOwnedTag(userId, id);

        String newName = req.name().trim();

        tagRepository.findTagIdByUserIdAndName(userId, newName)
                .filter(existingId -> !existingId.equals(id))
                .ifPresent(existingId -> {
                    throw new BusinessException(ErrorCode.TAG_NAME_DUPLICATED);
                });

        tag.setName(newName);

        long count = bookmarkTagRepository.countDistinctBookmarksByUserIdAndTagId(userId, id);
        return tagMapper.toResWithCount(tag, count);
    }

    @Transactional
    public void merge(Long userId, Long id, TagMergeReq req) {
        Long targetTagId = req.targetTagId();

        if(id.equals(targetTagId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        validateOwnedTag(userId, id);
        validateOwnedTag(userId, targetTagId);

        List<Long> bookmarkIds = bookmarkTagRepository.findAllBookmarkIdsByUserIdAndTagId(userId, id);
        if(bookmarkIds.isEmpty()) return;

        for(Long bookmarkId : bookmarkIds) {
            // 이미 targetId로 설정되어 있는 경우
            if(bookmarkTagRepository.existsByBookmark_IdAndTag_Id(bookmarkId, targetTagId)) {
                bookmarkTagRepository.deleteByBookmarkIdAndTagId(bookmarkId, id);
                continue;
            }
            // 없으면 id -> targetTagId 치환
            bookmarkTagRepository.replaceTagOnBookmark(bookmarkId, id, targetTagId);
        }
    }

    @Transactional
    public void delete(Long userId, Long id) {
        validateOwnedTag(userId, id);

        if(bookmarkTagRepository.existsByUserIdAndTagId(userId, id)) {
            throw new BusinessException(ErrorCode.TAG_IN_USE);
        }

        tagRepository.deleteById(id);
    }

    private void validateOwnedTag(Long userId, Long id) {
        boolean owned = bookmarkTagRepository.existsByUserIdAndTagId(userId, id);
        if(!owned) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Tag requireOwnedTag(Long userId, Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));

        validateOwnedTag(userId, id);
        return tag;
    }
}
