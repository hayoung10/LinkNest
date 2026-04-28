import http, { unwrap } from "@/api/http";
import type { Collection, CollectionNode, ID } from "@/types/common";
import type {
  CollectionNodeRes,
  CollectionPathRes,
  CollectionPositionRes,
  CollectionRes,
} from "./types";
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
  targetIndex: number;
}

export interface CollectionEmojiUpdateReq {
  emoji: string | null;
}

/** 생성 */
export async function createCollection(
  payload: CollectionCreateReq,
): Promise<Collection> {
  const data = await unwrap<CollectionRes>(http.post(`/collections`, payload));
  return mapCollectionRes(data);
}

/** 단건 조회 */
export async function getCollection(id: ID): Promise<Collection> {
  const data = await unwrap<CollectionRes>(http.get(`/collections/${id}`));
  return mapCollectionRes(data);
}

/** 수정 */
export async function updateCollection(
  id: ID,
  payload: CollectionUpdateReq,
): Promise<Collection> {
  const data = await unwrap<CollectionRes>(
    http.patch(`/collections/${id}`, payload),
  );

  // 북마크(children)는 호출부에서 기존 상태 유지
  return mapCollectionRes(data);
}

/** 이모지 수정 */
export async function updateCollectionEmoji(
  id: ID,
  payload: CollectionEmojiUpdateReq,
): Promise<Collection> {
  const data = await unwrap<CollectionRes>(
    http.patch(`/collections/${id}/emoji`, payload),
  );

  return mapCollectionRes(data);
}

/** 삭제 */
export async function deleteCollection(id: ID): Promise<void> {
  await unwrap<void>(http.delete(`/collections/${id}`));
}

/** 이동 */
export async function moveCollection(
  id: ID,
  payload: CollectionMoveReq,
): Promise<CollectionPositionRes> {
  const data = await unwrap<CollectionPositionRes>(
    http.patch(`/collections/${id}/move`, payload),
  );
  return data;
}

/** 정렬 변경 */
export async function reorderCollection(
  id: ID,
  payload: CollectionReorderReq,
): Promise<CollectionPositionRes> {
  const data = await unwrap<CollectionPositionRes>(
    http.patch(`/collections/${id}/reorder`, payload),
  );
  return data;
}

/** 트리 조회 */
export async function listTree(): Promise<CollectionNode[]> {
  const data = await unwrap<CollectionNodeRes[]>(http.get(`/collections/tree`));
  return mapCollectionNodeResList(data);
}

/** 경로 조회 */
export async function getPath(id: ID): Promise<CollectionPathRes[]> {
  const data = await unwrap<CollectionPathRes[]>(
    http.get(`/collections/${id}/path`),
  );
  return data;
}
