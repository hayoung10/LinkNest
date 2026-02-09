import http, { unwrap } from "@/api/http";
import type { ID, Tag, TaggedBookmark } from "@/types/common";
import type { TagRes } from "./types";
import type { SliceResponse } from "./common";
import { mapTaggedBookmarkRes, mapTagRes } from "./mappers";

export type TagSort = "NEWEST" | "OLDEST" | "NAME_ASC" | "COUNT_DESC";

export interface TagCreateReq {
  name: string;
}

export interface TagUpdateReq {
  name: string;
}

export interface TagMergeReq {
  targetTagId: ID;
}

export interface GetTagsParams {
  q?: string;
  sort?: TagSort;
  page?: number;
  size?: number;
}

export interface GetTaggedBookmarksParams {
  page?: number;
  size?: number;
}

export interface TagDetachReq {
  bookmarkIds: ID[];
}

export interface TagReplaceReq {
  targetTagId: ID;
  bookmarkIds: ID[];
}

/** 생성 */
export async function createTag(payload: TagCreateReq): Promise<Tag> {
  const data = await unwrap<TagRes>(http.post(`/tags`, payload));
  return mapTagRes(data);
}

/** 태그 목록 조회 */
export async function getTags(
  params: GetTagsParams = {},
): Promise<SliceResponse<Tag>> {
  const data = await unwrap<SliceResponse<TagRes>>(
    http.get(`/tags`, { params }),
  );
  return { items: data.items.map(mapTagRes), meta: data.meta };
}

/** 이름 변경 */
export async function renameTag(id: ID, payload: TagUpdateReq): Promise<Tag> {
  const data = await unwrap<TagRes>(http.patch(`/tags/${id}`, payload));
  return mapTagRes(data);
}

/** 태그 병합 */
export async function mergeTag(id: ID, payload: TagMergeReq): Promise<void> {
  await unwrap<void>(http.post(`/tags/${id}/merge`, payload));
}

/** 삭제 */
export async function deleteTag(id: ID): Promise<void> {
  await unwrap<void>(http.delete(`/tags/${id}`));
}

/** 태그된 북마크 목록 조회 (페이징) */
export async function getTaggedBookmarks(
  id: ID,
  params: GetTaggedBookmarksParams = {},
): Promise<SliceResponse<TaggedBookmark>> {
  const data = await unwrap<SliceResponse<TaggedBookmark>>(
    http.get(`/tags/${id}/bookmarks`, { params }),
  );
  return { items: data.items.map(mapTaggedBookmarkRes), meta: data.meta };
}

/** 선택한 북마크들에서 태그 제거 */
export async function detachTagFromBookmarks(
  id: ID,
  payload: TagDetachReq,
): Promise<void> {
  await unwrap<void>(http.post(`/tags/${id}/detach`, payload));
}

/** 선택한 북마크들에서 태그를 다른 태그로 교체 */
export async function replaceTagOnBookmarks(
  id: ID,
  payload: TagReplaceReq,
): Promise<void> {
  await unwrap<void>(http.post(`/tags/${id}/replace`, payload));
}
