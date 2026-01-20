import http, { unwrap } from "@/api/http";
import type { Bookmark, ID, ImageMode } from "@/types/common";
import type { BookmarkRes } from "./types";
import { mapBookmarkRes } from "./mappers";

export interface BookmarkCreateReq {
  collectionId: ID;
  url: string;
  title?: string | null;
  description?: string | null;
  emoji?: string | null;
  imageMode?: ImageMode;
  tags?: string[];
}

export interface BookmarkUpdateReq {
  url?: string;
  title?: string | null;
  description?: string | null;
  emoji?: string | null;
  tags?: string[];
}

export interface BookmarkMoveReq {
  targetCollectionId: ID;
}

export interface BookmarkImageModeUpdateReq {
  imageMode: Exclude<ImageMode, "CUSTOM">;
}

export interface BookmarkFavoriteUpdateReq {
  isFavorite: boolean;
}

/** 생성 */
export async function createBookmark(
  payload: BookmarkCreateReq,
): Promise<Bookmark> {
  const data = await unwrap<BookmarkRes>(http.post(`/bookmarks`, payload));
  return mapBookmarkRes(data);
}

/** 단건 조회 */
export async function getBookmark(id: ID): Promise<Bookmark> {
  const data = await unwrap<BookmarkRes>(http.get(`/bookmarks/${id}`));
  return mapBookmarkRes(data);
}

/** 수정 */
export async function updateBookmark(
  id: ID,
  payload: BookmarkUpdateReq,
): Promise<Bookmark> {
  const data = await unwrap<BookmarkRes>(
    http.patch(`/bookmarks/${id}`, payload),
  );
  return mapBookmarkRes(data);
}

/** 삭제 */
export async function deleteBookmark(id: ID): Promise<void> {
  await unwrap<void>(http.delete(`/bookmarks/${id}`));
}

/** 목록 */
export async function listBookmarks(collectionId?: ID): Promise<Bookmark[]> {
  const data = await unwrap<BookmarkRes[]>(
    http.get(`/bookmarks`, {
      params: collectionId == null ? {} : { collectionId },
    }),
  );
  return data.map(mapBookmarkRes);
}

/** 이동 */
export async function moveBookmark(
  id: ID,
  payload: BookmarkMoveReq,
): Promise<void> {
  await unwrap<void>(http.patch(`/bookmarks/${id}/move`, payload));
}

/** 북마크 커버 업로드 */
export async function uploadCover(id: ID, file: File): Promise<Bookmark> {
  const formData = new FormData();
  formData.append("file", file);

  const data = await unwrap<BookmarkRes>(
    http.post(`/bookmarks/${id}/cover`, formData),
  );

  return mapBookmarkRes(data);
}

/** 북마크 커버 삭제 */
export async function removeCover(id: ID): Promise<Bookmark> {
  const data = await unwrap<BookmarkRes>(http.delete(`/bookmarks/${id}/cover`));
  return mapBookmarkRes(data);
}

/** ImageMode 수정 */
export async function updateImageMode(
  id: ID,
  payload: BookmarkImageModeUpdateReq,
): Promise<Bookmark> {
  const data = await unwrap<BookmarkRes>(
    http.patch(`/bookmarks/${id}/image-mode`, payload),
  );
  return mapBookmarkRes(data);
}

/** 즐겨찾기 수정 */
export async function updateFavorite(
  id: ID,
  payload: BookmarkFavoriteUpdateReq,
): Promise<Bookmark> {
  const data = await unwrap<BookmarkRes>(
    http.patch(`/bookmarks/${id}/favorite`, payload),
  );
  return mapBookmarkRes(data);
}

/** 즐겨찾기 목록 조회 */
export async function listFavorites(): Promise<Bookmark[]> {
  const data = await unwrap<BookmarkRes[]>(http.get(`/bookmarks/favorites`));
  return data.map(mapBookmarkRes);
}
