export type ISODateTime = string;
export type ID = number;

// API 성공/실패 응답 규약
export type ApiSuccess<T> = {
  status: number;
  code: "OK";
  message: string;
  data: T;
};

export type ApiError = {
  status: number;
  code: string;
  message: string;
  path?: string;
  timestamp?: ISODateTime;
};

export type Role = "ROLE_USER" | "ROLE_ADMIN";
export type Provider = "GOOGLE" | "KAKAO" | "NAVER";

export interface User {
  id: ID;
  email: string | null;
  name: string | null;
  profileImageUrl: string | null;
  role: Role;
  provider: Provider;
  createdAt: ISODateTime;
  updatedAt: ISODateTime;
}

export type ImageMode = "AUTO" | "CUSTOM" | "NONE";

export interface Bookmark {
  id: ID;
  collectionId: ID;
  url: string;
  title: string | null;
  description: string | null;

  emoji: string | null;
  autoImageUrl: string | null;
  customImageUrl: string | null;
  imageMode: ImageMode;

  createdAt: ISODateTime;
  updatedAt: ISODateTime;
}

export interface Collection {
  id: ID;
  name: string;
  emoji: string | null;
  parentId: ID | null;
  sortOrder: number;

  createdAt: ISODateTime;
  updatedAt: ISODateTime;

  bookmarkCount?: number;
  childCount?: number;
  children?: Collection[];
  bookmarks?: Bookmark[];
}

export type BookmarkSortOption = "NEWEST" | "OLDEST" | "TITLE";
export type LayoutOption = "CARD" | "LIST";

export interface UserPreferences {
  defaultBookmarkSort: BookmarkSortOption;
  defaultLayout: LayoutOption;
  openInNewTab: boolean;
  keepSignedIn: boolean;
}
