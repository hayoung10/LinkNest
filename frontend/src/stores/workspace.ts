import { defineStore } from "pinia";
import { Bookmark, Collection, ID } from "@/types/common";
import * as CollectionApi from "@/api/collections";
import * as BookmarkApi from "@/api/bookmarks";

type LoadKey = "collections" | "bookmarks";
type MutateKey =
  | "createCollection"
  | "updateCollection"
  | "updateCollectionEmoji"
  | "deleteCollection"
  | "moveCollection"
  | "reorderCollection"
  | "createBookmark"
  | "updateBookmark"
  | "deleteBookmark"
  | "moveBookmark";
type ErrorMap<K extends string> = Record<K, string | null>;

interface WorkspaceState {
  collections: Collection[];
  bookmarks: Bookmark[];
  selectedCollectionId: ID | null;
  isLoading: Record<LoadKey, boolean>;
  error: Record<LoadKey, string | null>;
  isMutating: Record<MutateKey, boolean>;
  mutateError: Record<MutateKey, string | null>;
  loadingChildCollectionIds: Set<ID>;
}

function setLoading(
  target: Record<LoadKey, boolean>,
  key: LoadKey,
  isLoading: boolean
) {
  target[key] = isLoading;
}
function fail<K extends string>(
  target: ErrorMap<K>,
  key: K,
  e: unknown,
  fallbackMsg: string
) {
  target[key] = e instanceof Error && e.message ? e.message : fallbackMsg;
}
function setMutating(
  target: Record<MutateKey, boolean>,
  key: MutateKey,
  v: boolean
) {
  target[key] = v;
}

/* ------------ 컬렉션 유틸 (컬렉션 트리 관련) ------------ */
// 트리에서 특정 id를 가진 노드를 부분 업데이트
function updateNodeInTree(
  nodes: Collection[],
  id: ID,
  updated: Collection
): Collection[] {
  return nodes.map((node) => {
    if (node.id === id) {
      const {
        childCount: _ignoredCount,
        children: _ignoredChildren,
        ...rest
      } = updated;

      return {
        ...node,
        ...rest,
        children: node.children,
        childCount: node.childCount,
      };
    }

    if (node.children?.length) {
      return {
        ...node,
        children: updateNodeInTree(node.children, id, updated),
      };
    }
    return node;
  });
}
// 트리에서 특정 id를 가진 노드를 삭제
function removeNodeFromTree(nodes: Collection[], id: ID): Collection[] {
  return nodes
    .filter((node) => node.id !== id)
    .map((node) => {
      if (node.children?.length) {
        const nextChildren = removeNodeFromTree(node.children, id);

        return {
          ...node,
          children: nextChildren,
          childCount: nextChildren.length,
        };
      }
      return node;
    });
}
// 트리에서 특정 id를 가진 노드의 children을 교체
function updateChildrenInTree(
  nodes: Collection[],
  id: ID,
  children: Collection[]
): Collection[] {
  return nodes.map((node) => {
    if (node.id === id) {
      return {
        ...node,
        children,
        childCount: children.length,
      };
    }

    if (node.children?.length) {
      return {
        ...node,
        children: updateChildrenInTree(node.children, id, children),
      };
    }
    return node;
  });
}
// 트리에서 특정 parentId를 가진 노드에 자식을 추가
function addChildToTree(
  nodes: Collection[],
  parentId: ID,
  child: Collection
): Collection[] {
  return nodes.map((node) => {
    if (node.id === parentId) {
      return {
        ...node,
        children: [...(node.children ?? []), child],
        childCount: (node.childCount ?? 0) + 1,
      };
    }

    if (node.children?.length) {
      return {
        ...node,
        children: addChildToTree(node.children, parentId, child),
      };
    }
    return node;
  });
}
// 트리에서 특정 collectionId를 가진 노드의 bookmarkCount를 cnt만큼 변경
function updateBookmarkCountInTree(
  nodes: Collection[],
  collectionId: ID,
  cnt: number
): Collection[] {
  return nodes.map((node) => {
    if (node.id === collectionId) {
      return {
        ...node,
        bookmarkCount: Math.max(0, (node.bookmarkCount ?? 0) + cnt),
      };
    }

    if (node.children?.length) {
      return {
        ...node,
        children: updateBookmarkCountInTree(node.children, collectionId, cnt),
      };
    }
    return node;
  });
}

export const useWorkspaceStore = defineStore("workspace", {
  state: (): WorkspaceState => ({
    collections: [],
    bookmarks: [],
    selectedCollectionId: null,
    isLoading: { collections: false, bookmarks: false },
    error: { collections: null, bookmarks: null },
    isMutating: {
      createCollection: false,
      updateCollection: false,
      updateCollectionEmoji: false,
      deleteCollection: false,
      moveCollection: false,
      reorderCollection: false,
      createBookmark: false,
      updateBookmark: false,
      deleteBookmark: false,
      moveBookmark: false,
    },
    mutateError: {
      createCollection: null,
      updateCollection: null,
      updateCollectionEmoji: null,
      deleteCollection: null,
      moveCollection: null,
      reorderCollection: null,
      createBookmark: null,
      updateBookmark: null,
      deleteBookmark: null,
      moveBookmark: null,
    },
    loadingChildCollectionIds: new Set<ID>(),
  }),

  actions: {
    // 초기화
    resetAll() {
      this.collections = [];
      this.bookmarks = [];
      this.selectedCollectionId = null;
      this.isLoading = { collections: false, bookmarks: false };
      this.error = { collections: null, bookmarks: null };
      this.isMutating = {
        createCollection: false,
        updateCollection: false,
        updateCollectionEmoji: false,
        deleteCollection: false,
        moveCollection: false,
        reorderCollection: false,
        createBookmark: false,
        updateBookmark: false,
        deleteBookmark: false,
        moveBookmark: false,
      };
      this.mutateError = {
        createCollection: null,
        updateCollection: null,
        updateCollectionEmoji: null,
        deleteCollection: null,
        moveCollection: null,
        reorderCollection: null,
        createBookmark: null,
        updateBookmark: null,
        deleteBookmark: null,
        moveBookmark: null,
      };
    },
    selectCollection(id: ID | null) {
      this.selectedCollectionId = id;
    },
    // 북마크 카운트 업데이트
    updateBookmarkCount(collectionId: ID, cnt: number) {
      if (!this.collections?.length) return;

      this.collections = updateBookmarkCountInTree(
        this.collections,
        collectionId,
        cnt
      );
    },

    /* ------------------------ 컬렉션 ------------------------ */
    async createCollection(payload: {
      name: string;
      emoji: string | null;
      parentId: ID | null;
    }) {
      this.mutateError.createCollection = null;
      setMutating(this.isMutating, "createCollection", true);
      try {
        const created = await CollectionApi.createCollection(payload);
        const parentId = payload.parentId ?? null;

        if (parentId === null) {
          this.collections.push(created); // 루트 컬렉션 추가
          return;
        }

        this.collections = addChildToTree(this.collections, parentId, created); // 하위 컬렉션 추가
      } catch (e) {
        fail(
          this.mutateError,
          "createCollection",
          e,
          "컬렉션 생성에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "createCollection", false);
      }
    },

    async getCollection(id: ID) {
      this.error.collections = null;
      setLoading(this.isLoading, "collections", true);
      try {
        const collection = await CollectionApi.getCollection(id);
        this.collections = updateNodeInTree(this.collections, id, collection);
      } catch (e) {
        fail(
          this.error,
          "collections",
          e,
          "컬렉션 정보를 불러오지 못했습니다."
        );
        throw e;
      } finally {
        setLoading(this.isLoading, "collections", false);
      }
    },

    async updateCollection(id: ID, payload: { name?: string }) {
      this.mutateError.updateCollection = null;
      setMutating(this.isMutating, "updateCollection", true);
      try {
        const updated = await CollectionApi.updateCollection(id, payload);
        this.collections = updateNodeInTree(this.collections, id, updated);
      } catch (e) {
        fail(
          this.mutateError,
          "updateCollection",
          e,
          "컬렉션 수정에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "updateCollection", false);
      }
    },

    async updateCollectionEmoji(id: ID, payload: { emoji: string | null }) {
      this.mutateError.updateCollectionEmoji = null;
      setMutating(this.isMutating, "updateCollectionEmoji", true);
      try {
        const updated = await CollectionApi.updateCollectionEmoji(id, payload);
        this.collections = updateNodeInTree(this.collections, id, updated);
      } catch (e) {
        fail(
          this.mutateError,
          "updateCollectionEmoji",
          e,
          "컬렉션 이모지 수정에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "updateCollectionEmoji", false);
      }
    },

    async deleteCollection(id: ID) {
      this.mutateError.deleteCollection = null;
      setMutating(this.isMutating, "deleteCollection", true);
      try {
        await CollectionApi.deleteCollection(id);
        this.collections = removeNodeFromTree(this.collections, id);

        // 선택된 컬렉션을 제거한 경우
        if (this.selectedCollectionId === id) {
          this.selectedCollectionId = null;
          this.bookmarks = [];
        }
      } catch (e) {
        fail(
          this.mutateError,
          "deleteCollection",
          e,
          "컬렉션 삭제에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "deleteCollection", false);
      }
    },

    async fetchCollections(parentId?: ID | null) {
      this.error.collections = null;
      setLoading(this.isLoading, "collections", true);
      try {
        const children = await CollectionApi.listChildren(parentId);

        if (parentId == null) {
          this.collections = children;
        } else {
          this.collections = updateChildrenInTree(
            this.collections,
            parentId,
            children
          );
        }
      } catch (e) {
        fail(
          this.error,
          "collections",
          e,
          "컬렉션 목록을 불러오지 못했습니다."
        );
        throw e;
      } finally {
        setLoading(this.isLoading, "collections", false);
      }
    },

    async fetchChildCollections(id: ID) {
      this.error.collections = null;

      if (this.loadingChildCollectionIds.has(id)) return;
      this.loadingChildCollectionIds = new Set(
        this.loadingChildCollectionIds
      ).add(id);

      try {
        const children = await CollectionApi.listChildren(id);
        this.collections = updateChildrenInTree(this.collections, id, children);
      } catch (e) {
        fail(
          this.error,
          "collections",
          e,
          "하위 컬렉션 목록을 불러오지 못했습니다."
        );
        throw e;
      } finally {
        const next = new Set(this.loadingChildCollectionIds);
        next.delete(id);
        this.loadingChildCollectionIds = next;
      }
    },

    async moveCollection(id: ID, targetParentId: ID | null) {
      this.mutateError.moveCollection = null;
      setMutating(this.isMutating, "moveCollection", true);
      try {
        await CollectionApi.moveCollection(id, { targetParentId });

        // TODO: 현재 전체 트리를 다시 가져오는 방식으로 수정
        await this.fetchCollections(null);
      } catch (e) {
        fail(
          this.mutateError,
          "moveCollection",
          e,
          "컬렉션 이동에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "moveCollection", false);
      }
    },

    async reorderCollection(id: ID, parentId: ID | null, newOrder: number) {
      this.mutateError.reorderCollection = null;
      setMutating(this.isMutating, "reorderCollection", true);
      try {
        await CollectionApi.reorderCollection(id, { newOrder });
        await this.fetchCollections(parentId);
      } catch (e) {
        fail(
          this.mutateError,
          "reorderCollection",
          e,
          "컬렉션 정렬에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "reorderCollection", false);
      }
    },

    /* ------------------------ 북마크 ------------------------ */
    async createBookmark(payload: BookmarkApi.BookmarkCreateReq) {
      this.mutateError.createBookmark = null;
      setMutating(this.isMutating, "createBookmark", true);
      try {
        const created = await BookmarkApi.createBookmark(payload);
        if (this.selectedCollectionId === created.collectionId) {
          this.bookmarks.push(created);
        }
        this.updateBookmarkCount(created.collectionId, +1);
      } catch (e) {
        fail(
          this.mutateError,
          "createBookmark",
          e,
          "북마크 생성에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "createBookmark", false);
      }
    },

    async getBookmark(id: ID) {
      this.error.bookmarks = null;
      setLoading(this.isLoading, "bookmarks", true);
      try {
        const bookmark = await BookmarkApi.getBookmark(id);
        const idx = this.bookmarks.findIndex((b) => b.id === id);
        if (idx >= 0) this.bookmarks.splice(idx, 1, bookmark);
        else this.bookmarks.push(bookmark);
      } catch (e) {
        fail(this.error, "bookmarks", e, "북마크 정보를 불러오지 못했습니다.");
        throw e;
      } finally {
        setLoading(this.isLoading, "bookmarks", false);
      }
    },

    async updateBookmark(id: ID, payload: BookmarkApi.BookmarkUpdateReq) {
      this.mutateError.updateBookmark = null;
      setMutating(this.isMutating, "updateBookmark", true);
      try {
        const updated = await BookmarkApi.updateBookmark(id, payload);
        const idx = this.bookmarks.findIndex((b) => b.id === id);
        if (idx >= 0) this.bookmarks.splice(idx, 1, updated);
      } catch (e) {
        fail(
          this.mutateError,
          "updateBookmark",
          e,
          "북마크 수정에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "updateBookmark", false);
      }
    },

    async deleteBookmark(id: ID) {
      this.mutateError.deleteBookmark = null;
      setMutating(this.isMutating, "deleteBookmark", true);
      try {
        const target = this.bookmarks.find((b) => b.id === id);
        await BookmarkApi.deleteBookmark(id);
        this.bookmarks = this.bookmarks.filter((b) => b.id !== id);
        if (target) this.updateBookmarkCount(target.collectionId, -1);
      } catch (e) {
        fail(
          this.mutateError,
          "deleteBookmark",
          e,
          "북마크 삭제에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "deleteBookmark", false);
      }
    },

    async fetchBookmarks(collectionId?: ID) {
      this.error.bookmarks = null;
      setLoading(this.isLoading, "bookmarks", true);
      try {
        const cid = collectionId ?? this.selectedCollectionId ?? null;
        if (cid == null) {
          this.bookmarks = [];
          return;
        }
        this.bookmarks = await BookmarkApi.listBookmarks(cid);
      } catch (e) {
        fail(this.error, "bookmarks", e, "북마크 목록을 불러오지 못했습니다.");
        throw e;
      } finally {
        setLoading(this.isLoading, "bookmarks", false);
      }
    },

    async moveBookmark(id: ID, targetCollectionId: ID) {
      this.mutateError.moveBookmark = null;
      setMutating(this.isMutating, "moveBookmark", true);
      try {
        await BookmarkApi.moveBookmark(id, { targetCollectionId });
        // TODO: 북마크 이동 시 카운트 변경

        if (this.selectedCollectionId != null) {
          await this.fetchBookmarks(this.selectedCollectionId);
        } else {
          this.bookmarks = [];
        }
      } catch (e) {
        fail(
          this.mutateError,
          "moveBookmark",
          e,
          "북마크 이동에 실패했습니다."
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "moveBookmark", false);
      }
    },

    replaceBookmark(updated: Bookmark) {
      const idx = this.bookmarks.findIndex((b) => b.id === updated.id);
      if (idx >= 0) this.bookmarks.splice(idx, 1, updated);
    },
  },
});
