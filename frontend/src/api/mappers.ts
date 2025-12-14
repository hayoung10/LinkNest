import { Bookmark, Collection, User, UserPreferences } from "@/types/common";
import {
  BookmarkRes,
  CollectionRes,
  UserPreferencesRes,
  UserRes,
} from "./types";

/** === 백엔드 응답 DTO -> 도메인 모델 매핑 === */
export function mapUserRes(dto: UserRes): User {
  return {
    id: dto.id,
    email: dto.email,
    name: dto.name ?? null,
    profileImageUrl: dto.profileImageUrl,
    role: dto.role,
    provider: dto.provider,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  };
}

export function mapBookmarkRes(dto: BookmarkRes): Bookmark {
  return {
    id: dto.id,
    collectionId: dto.collectionId,
    url: dto.url,
    title: dto.title ?? null,
    description: dto.description ?? null,

    emoji: dto.emoji,
    autoImageUrl: dto.autoImageUrl ?? null,
    customImageUrl: dto.customImageUrl ?? null,
    imageMode: dto.imageMode ?? "AUTO",

    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  };
}

export function mapCollectionRes(dto: CollectionRes): Collection {
  return {
    id: dto.id,
    name: dto.name,
    emoji: dto.emoji,
    parentId: dto.parentId,
    sortOrder: dto.sortOrder,

    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,

    bookmarkCount: dto.bookmarkCount,
    childCount: dto.childCount,
  };
}

export function mapUserPreferences(dto: UserPreferencesRes): UserPreferences {
  const sort = dto.defaultBookmarkSort;
  const layout = dto.defaultLayout;

  return {
    defaultBookmarkSort:
      sort === "NEWEST" || sort === "OLDEST" || sort === "TITLE"
        ? sort
        : "NEWEST",
    defaultLayout: layout === "CARD" || layout === "LIST" ? layout : "LIST",
    openInNewTab: dto.openInNewTab,
    keepSignedIn: dto.keepSignedIn,
  };
}
