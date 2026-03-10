import {
  Bookmark,
  Collection,
  CollectionNode,
  Tag,
  TaggedBookmark,
  TrashItem,
  User,
  UserPreferences,
} from "@/types/common";
import {
  BookmarkRes,
  CollectionNodeRes,
  CollectionRes,
  TaggedBookmarkRes,
  TagRes,
  TrashItemRes,
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
    autoImageStatus: dto.autoImageStatus ?? null,

    isFavorite: dto.isFavorite ?? false,
    tags: dto.tags ?? [],

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

export function mapCollectionNodeRes(dto: CollectionNodeRes): CollectionNode {
  return {
    id: dto.id,
    name: dto.name,
    emoji: dto.emoji,
    parentId: dto.parentId,
    sortOrder: dto.sortOrder,

    bookmarkCount: dto.bookmarkCount,
    childCount: dto.childCount,
  };
}

export function mapCollectionNodeResList(
  list: CollectionNodeRes[] | null | undefined,
): CollectionNode[] {
  if (!list?.length) return [];
  return list.map(mapCollectionNodeRes);
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

export function mapTagRes(dto: TagRes): Tag {
  return {
    id: dto.id,
    name: dto.name,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
    bookmarkCount: dto.bookmarkCount,
  };
}

export function mapTaggedBookmarkRes(dto: TaggedBookmarkRes): TaggedBookmark {
  return {
    id: dto.id,

    collectionId: dto.collectionId,
    collectionName: dto.collectionName,
    collectionEmoji: dto.collectionEmoji,

    url: dto.url,
    title: dto.title ?? null,
    description: dto.description ?? null,

    emoji: dto.emoji,
    autoImageUrl: dto.autoImageUrl ?? null,
    customImageUrl: dto.customImageUrl ?? null,
    imageMode: dto.imageMode ?? "AUTO",
    autoImageStatus: dto.autoImageStatus ?? null,

    isFavorite: dto.isFavorite ?? false,
    tags: dto.tags ?? [],

    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  };
}

export function mapTrashItemRes(dto: TrashItemRes): TrashItem {
  return {
    type: dto.type,
    id: dto.id,

    title: dto.title,
    subtitle: dto.subtitle ?? null,
    emoji: dto.emoji ?? null,

    parentName: dto.parentName ?? null,
    parentEmoji: dto.parentEmoji ?? null,

    deletedAt: dto.deletedAt,

    childCount: dto.childCount ?? null,
    bookmarkCount: dto.bookmarkCount ?? null,
    taggedCount: dto.taggedCount ?? null,
  };
}

/** === TaggedBookmark -> Bookmark 매핑 === */
export function toBookmarkFromTagged(b: TaggedBookmark): Bookmark {
  return {
    id: b.id,
    collectionId: b.collectionId,
    url: b.url,
    title: b.title,
    description: b.description,
    emoji: b.emoji,
    autoImageUrl: b.autoImageUrl,
    customImageUrl: b.customImageUrl,
    imageMode: b.imageMode,
    autoImageStatus: b.autoImageStatus ?? null,
    isFavorite: b.isFavorite,
    tags: b.tags ?? [],
    createdAt: b.createdAt,
    updatedAt: b.updatedAt,
  };
}
