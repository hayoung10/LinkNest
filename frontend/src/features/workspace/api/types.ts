import type { Bookmark, Collection } from "@/types/common";

/** === 백엔드 응답 DTO (Res) === */
export interface BookmarkRes {
  id: number;
  collectionId: number;
  url: string;
  title: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface CollectionRes {
  id: number;
  name: string;
  icon: string | null;
  parentId: number | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  bookmarkCount: number;
}

/** === 매퍼: Res -> UI 모델 === */
export const mapBookmarkRes = (r: BookmarkRes): Bookmark => ({
  id: r.id,
  collectionId: r.collectionId,
  url: r.url,
  title: r.title,
  description: r.description,
  createdAt: r.createdAt,
  updatedAt: r.updatedAt,
});

export const mapCollectionRes = (
  r: CollectionRes,
  bookmarks: Bookmark[] = []
): Collection => ({
  id: r.id,
  name: r.name,
  icon: r.icon,
  parentId: r.parentId,
  sortOrder: r.sortOrder,
  createdAt: r.createdAt,
  updatedAt: r.updatedAt,
  bookmarkCount: r.bookmarkCount,
  bookmarks,
});
