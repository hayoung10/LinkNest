import type { ID, Role } from "@/types/common";

/** === 백엔드 응답 DTO (Res) === */
export interface UserRes {
  id: ID;
  name: string;
  email: string;
  profileImageUrl: string | null;
  role: Role;
  createdAt: string;
  updatedAt: string;
  bookmarkCount: number;
  collectionCount: number;
}

export interface BookmarkRes {
  id: ID;
  collectionId: ID;
  url: string;
  title: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface CollectionRes {
  id: ID;
  name: string;
  icon: string | null;
  parentId: ID | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  bookmarkCount: number;
  childCount: number;
}

export interface UserPreferencesRes {
  defaultBookmarkSort: string;
  defaultLayout: string;
  openInNewTab: boolean;
  keepSignedIn: boolean;
}
