import type {
  BookmarkSortOption,
  ID,
  ImageMode,
  LayoutOption,
  Provider,
  Role,
} from "@/types/common";

/** === 백엔드 응답 DTO (Res) === */
export interface UserRes {
  id: ID;
  name: string | null;
  email: string;
  profileImageUrl: string | null;
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
  imageMode: ImageMode | null;

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

export interface UserPreferencesRes {
  defaultBookmarkSort: BookmarkSortOption;
  defaultLayout: LayoutOption;
  openInNewTab: boolean;
  keepSignedIn: boolean;
}
