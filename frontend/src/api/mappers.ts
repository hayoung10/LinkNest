import { Bookmark, Collection } from "@/types/common";
import { BookmarkRes, CollectionRes } from "./types";

/** === 백엔드 응답 DTO -> 도메인 모델 매핑 === */

export function mapBookmarkRes(dto: BookmarkRes): Bookmark {
  return {
    id: dto.id,
    collectionId: dto.collectionId,
    url: dto.url,
    title: dto.title ?? null,
    description: dto.description ?? null,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  };
}

export function mapCollectionRes(dto: CollectionRes): Collection {
  return {
    id: dto.id,
    name: dto.name,
    icon: dto.icon ?? null,
    parentId: dto.parentId ?? null,
    sortOrder: dto.sortOrder,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
    bookmarkCount: dto.bookmarkCount,
    childCount: dto.childCount,
  };
}
