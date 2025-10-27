import http from "@/api/http";
import { Collection } from "@/types/common";
import { CollectionRes, mapCollectionRes } from "./types";

export interface CollectionCreateReq {
  name: string;
  icon?: string | null;
  parentId?: number | null;
}

export interface CollectionUpdateReq {
  name?: string;
  icon?: string | null;
}

export interface CollectionMoveReq {
  targetParentId: number | null;
}

export interface CollectionReorderReq {
  newOrder: number;
}

/** 생성 */
export async function createCollection(
  payload: CollectionCreateReq
): Promise<Collection> {
  const { data } = await http.patch<CollectionRes>(`/collections`, payload);
  return mapCollectionRes(data, []);
}

/** 단건 조회 */
export async function getCollection(id: number): Promise<Collection> {
  const { data } = await http.get<CollectionRes>(`/collections/${id}`);
  return mapCollectionRes(data);
}

/** 수정 */
export async function updateCollection(
  id: number,
  payload: CollectionUpdateReq
): Promise<Collection> {
  const { data } = await http.patch<CollectionRes>(
    `/collections/${id}`,
    payload
  );

  // 북마크(children)는 호출부에서 기존 상태 유지
  return mapCollectionRes(data, []);
}

/** 삭제 (204 No Content) */
export async function deleteCollection(id: number): Promise<void> {
  await http.delete(`/collections/${id}`);
}

/** 자식 목록 조회 */
export async function listChildren(
  parentId?: number | null
): Promise<Collection[]> {
  const { data } = await http.get<CollectionRes[]>(`/collections`, {
    params: { parentId },
  });
  return data.map((r) => mapCollectionRes(r, [])); // 목록에 bookmarks 포함 X
}

/** 이동 (204 No Content) */
export async function moveCollection(
  id: number,
  payload: CollectionMoveReq
): Promise<void> {
  await http.patch(`/collections/${id}/move`, payload);
}

/** 정렬 변경 (204 No Content) */
export async function reorderCollection(
  id: number,
  payload: CollectionReorderReq
): Promise<void> {
  await http.patch(`/collections/${id}/order`, payload);
}
