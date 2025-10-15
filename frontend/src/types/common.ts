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
  timestamp?: string;
};

export type Role = "ROLE_USER" | "ROLE_ADMIN";

export interface User {
  id: number;
  name: string;
  profileImageUrl: string | null;
  role: Role;
  createdAt: string;
  updatedAt: string;
  bookmarkCount: number;
  collectionCount: number;
}

/** 화면/스토어에서 사용 */
export interface Bookmark {
  id: number;
  collectionId: number;
  url: string;
  title: string;
  description: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Collection {
  id: number;
  name: string;
  icon?: string | null;
  parentId?: number | null;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
  bookmarkCount?: number;

  bookmarks?: Bookmark[];
}
