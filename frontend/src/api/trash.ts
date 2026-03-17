import http, { unwrap } from "./http";
import type {
  ApiSuccess,
  ID,
  TrashBulkItem,
  TrashItem,
  TrashType,
} from "@/types/common";
import type { TrashItemRes } from "./types";
import type { SliceResponse } from "./common";
import { mapTrashItemRes } from "./mappers";

export interface TrashBulkReq {
  ids: ID[];
}

export interface GetTrashParams {
  type?: TrashType;
  page?: number;
  size?: number;
}

export interface TrashMixedBulkReq {
  items: TrashBulkItem[];
}

/** 휴지통 목록 조회 */
export async function getTrash(
  params: GetTrashParams = {},
): Promise<SliceResponse<TrashItem>> {
  const data = await unwrap<SliceResponse<TrashItemRes>>(
    http.get<ApiSuccess<SliceResponse<TrashItemRes>>>(`/trash`, { params }),
  );
  return {
    items: data.items.map(mapTrashItemRes),
    meta: data.meta,
  };
}

/** 휴지통 비우기 */
export async function emptyTrash(type?: TrashType): Promise<void> {
  await unwrap<void>(
    http.delete<ApiSuccess<void>>(`/trash/empty`, {
      params: type ? { type } : undefined,
    }),
  );
}

/** 단일 복구 */
export async function restoreTrashItem(type: TrashType, id: ID): Promise<void> {
  await unwrap<void>(
    http.post<ApiSuccess<void>>(`/trash/${type}/${id}/restore`),
  );
}

/** 단일 영구 삭제 */
export async function deleteTrashItem(type: TrashType, id: ID): Promise<void> {
  await unwrap<void>(http.delete<ApiSuccess<void>>(`/trash/${type}/${id}`));
}

/** 선택 항목 복구 (mixed bulk) */
export async function restoreMixedBulk(
  payload: TrashMixedBulkReq,
): Promise<void> {
  await unwrap<void>(http.post<ApiSuccess<void>>(`/trash/restore`, payload));
}

/** 선택 항목 영구 삭제 (mixed bulk) */
export async function deleteMixedBulk(
  payload: TrashMixedBulkReq,
): Promise<void> {
  await unwrap<void>(http.post<ApiSuccess<void>>(`/trash/delete`, payload));
}

/** 선택 항목 복구 (bulk) */
export async function restoreTrashBulk(
  type: TrashType,
  payload: TrashBulkReq,
): Promise<void> {
  await unwrap<void>(
    http.post<ApiSuccess<void>>(`/trash/${type}/restore`, payload),
  );
}

/** 선택 항목 영구 삭제 (bulk) */
export async function deleteTrashBulk(
  type: TrashType,
  payload: TrashBulkReq,
): Promise<void> {
  await unwrap<void>(
    http.post<ApiSuccess<void>>(`/trash/${type}/delete`, payload),
  );
}
