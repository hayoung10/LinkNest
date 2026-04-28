import type {
  AutoImageStatus,
  BookmarkSortOption,
  ID,
  ImageMode,
  LayoutOption,
  Provider,
  Role,
  Tag,
  TrashType,
} from "@/types/common";

/** === 백엔드 응답 DTO (Res) === */
export interface UserRes {
  id: ID;
  name: string | null;
  email: string;
  profileImageUrl: string | null;
  hasCustomProfileImage: boolean;
  role: Role;
  provider: Provider;
  createdAt: string;
  updatedAt: string;
}

export interface BookmarkRes {
  id: ID;
  collectionId: ID;
  url: string;
  title: string | null;
  description: string | null;

  emoji: string | null;
  autoImageUrl: string | null;
  customImageUrl: string | null;
  imageMode: ImageMode;
  autoImageStatus: AutoImageStatus | null;

  isFavorite: boolean;
  tags: string[];

  createdAt: string;
  updatedAt: string;
}

export interface CollectionRes {
  id: ID;
  name: string;
  emoji: string | null;
  parentId: ID | null;
  sortOrder: number;

  createdAt: string;
  updatedAt: string;

  bookmarkCount: number;
  childCount: number;
}

export interface CollectionNodeRes {
  id: ID;
  name: string;
  emoji: string | null;
  parentId: ID | null;
  sortOrder: number;

  bookmarkCount: number;
  childCount: number;
}

export interface CollectionPathRes {
  id: ID;
  name: string;
  emoji: string | null;
}

export interface CollectionPositionRes {
  id: ID;
  parentId: ID | null;
  sortOrder: number;
}

export interface UserPreferencesRes {
  defaultBookmarkSort: BookmarkSortOption;
  defaultLayout: LayoutOption;
  openInNewTab: boolean;
  keepSignedIn: boolean;
}

export interface TagRes {
  id: ID;
  name: string;
  createdAt: string;
  updatedAt: string;
  bookmarkCount: number;
}

export interface TaggedBookmarkRes {
  id: ID;

  collectionId: ID;
  collectionName: string;
  collectionEmoji: string | null;

  url: string;
  title: string | null;
  description: string | null;

  emoji: string | null;
  autoImageUrl: string | null;
  customImageUrl: string | null;
  imageMode: ImageMode;
  autoImageStatus: AutoImageStatus | null;

  isFavorite: boolean;
  tags: string[];

  createdAt: string;
  updatedAt: string;
}

export interface TagSummaryRes {
  totalTags: number;
  totalTaggedBookmarks: number;
}

export type TagCreateResult = {
  tag: Tag;
  restored: boolean;
};

export type TrashItemRes = {
  type: TrashType;
  id: ID;

  title: string;
  subtitle?: string | null;
  emoji?: string | null;

  parentName?: string | null;
  parentEmoji?: string | null;

  deletedAt: string;

  childCount?: number | null; // COLLECTION only
  bookmarkCount?: number | null; // COLLECTION only
  taggedCount?: number | null; // TAG only
};
