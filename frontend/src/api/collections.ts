import http from "@/api/http";
import type { Collection, CollectionNode, ID } from "@/types/common";
import { CollectionRes } from "./types";
import { mapCollectionNodeResList, mapCollectionRes } from "./mappers";

export interface CollectionCreateReq {
  name: string;
  emoji: string | null;
  parentId: ID | null;
}

export interface CollectionUpdateReq {
  name?: string;
}

export interface CollectionMoveReq {
  targetParentId: ID | null;
}

export interface CollectionReorderReq {
  newOrder: number;
}

export interface CollectionEmojiUpdateReq {
  emoji: string | null;
}

/** 생성 */
export async function createCollection(
  payload: CollectionCreateReq
): Promise<Collection> {
  const { data } = await http.post<CollectionRes>(`/collections`, payload);
  return mapCollectionRes(data);
}

/** 단건 조회 */
export async function getCollection(id: ID): Promise<Collection> {
  const { data } = await http.get<CollectionRes>(`/collections/${id}`);
  return mapCollectionRes(data);
}

/** 수정 */
export async function updateCollection(
  id: ID,
  payload: CollectionUpdateReq
): Promise<Collection> {
  const { data } = await http.patch<CollectionRes>(
    `/collections/${id}`,
    payload
  );

  // 북마크(children)는 호출부에서 기존 상태 유지
  return mapCollectionRes(data);
}

/** 이모지 수정 */
export async function updateCollectionEmoji(
  id: ID,
  payload: CollectionEmojiUpdateReq
): Promise<Collection> {
  const { data } = await http.patch<CollectionRes>(
    `/collections/${id}/emoji`,
    payload
  );

  return mapCollectionRes(data);
}

/** 삭제 (204 No Content) */
export async function deleteCollection(id: ID): Promise<void> {
  await http.delete(`/collections/${id}`);
}

/** 이동 (204 No Content) */
export async function moveCollection(
  id: ID,
  payload: CollectionMoveReq
): Promise<void> {
  await http.patch(`/collections/${id}/move`, payload);
}

/** 정렬 변경 (204 No Content) */
export async function reorderCollection(
  id: ID,
  payload: CollectionReorderReq
): Promise<void> {
  await http.patch(`/collections/${id}/order`, payload);
}

/** 트리 조회 */
export async function listTree(): Promise<CollectionNode[]> {
  const { data } = await http.get(`/collections/tree`);
  return mapCollectionNodeResList(data);
}
