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

export interface User {
  id: ID;
  name: string;
  profileImageUrl?: string | null;
  role: Role;
  createdAt: ISODateTime;
  updatedAt: ISODateTime;
  bookmarkCount?: number;
  collectionCount?: number;
}

export interface Bookmark {
  id: ID;
  collectionId: ID;
  url: string;
  title?: string | null;
  description?: string | null;
  createdAt: ISODateTime;
  updatedAt: ISODateTime;
}

export interface Collection {
  id: ID;
  name: string;
  icon?: string | null;
  parentId?: ID | null;
  sortOrder?: number;
  createdAt: ISODateTime;
  updatedAt: ISODateTime;
  bookmarkCount?: number;
  children?: Collection[];
  bookmarks?: Bookmark[];
}
