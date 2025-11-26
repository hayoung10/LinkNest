import http from "@/api/http";
import type { Bookmark, ID } from "@/types/common";
import { BookmarkRes } from "./types";
import { mapBookmarkRes } from "./mappers";

export interface BookmarkCreateReq {
  collectionId: ID;
  url: string;
  title?: string;
  description?: string;
}

export interface BookmarkUpdateReq {
  url?: string;
  title?: string | null;
  description?: string | null;
}

export interface BookmarkMoveReq {
  targetCollectionId: ID;
}

/** 생성 */
export async function createBookmark(
  payload: BookmarkCreateReq
): Promise<Bookmark> {
  const { data } = await http.post<BookmarkRes>(`/bookmarks`, payload);
  return mapBookmarkRes(data);
}

/** 단건 조회 */
export async function getBookmark(id: ID): Promise<Bookmark> {
  const { data } = await http.get<BookmarkRes>(`/bookmarks/${id}`);
  return mapBookmarkRes(data);
}

/** 수정 */
export async function updateBookmark(
  id: ID,
  payload: BookmarkUpdateReq
): Promise<Bookmark> {
  const { data } = await http.patch<BookmarkRes>(`/bookmarks/${id}`, payload);
  return mapBookmarkRes(data);
}

/** 삭제 (204 No Content) */
export async function deleteBookmark(id: ID): Promise<void> {
  await http.delete(`/bookmarks/${id}`);
}

/** 목록 */
export async function listBookmarks(collectionId?: ID): Promise<Bookmark[]> {
  const { data } = await http.get<BookmarkRes[]>(`/bookmarks`, {
    params: { collectionId },
  });
  return data.map(mapBookmarkRes);
}

/** 이동 (204 No Content) */
export async function moveBookmark(
  id: ID,
  payload: BookmarkMoveReq
): Promise<void> {
  await http.patch(`/bookmarks/${id}/move`, payload);
}
