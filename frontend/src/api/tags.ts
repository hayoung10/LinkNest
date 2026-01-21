import http, { unwrap } from "@/api/http";
import type { ID, Tag } from "@/types/common";
import type { TagRes } from "./types";
import { mapTagRes } from "./mappers";

export type TagSort =
  | "NEWEST"
  | "OLDEST"
  | "NAME_ASC"
  | "NAME_DESC"
  | "COUNT_DESC"
  | "COUNT_ASC";

export interface TagUpdateReq {
  name: string;
}

export interface TagMergeReq {
  targetTagId: ID;
}

export type PageMeta = {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PageResponse<T> = {
  items: T[];
  meta: PageMeta;
};

export interface GetTagsParams {
  q?: string;
  sort?: TagSort;
  page?: number;
  size?: number;
}

/** 태그 목록 조회 */
export async function getTags(
  params: GetTagsParams = {},
): Promise<PageResponse<Tag>> {
  const data = await unwrap<PageResponse<TagRes>>(
    http.get(`/tags`, { params }),
  );

  return {
    items: data.items.map(mapTagRes),
    meta: data.meta,
  };
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
