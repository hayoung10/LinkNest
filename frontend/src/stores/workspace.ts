import { defineStore } from "pinia";
import { Bookmark, CollectionNode, ID } from "@/types/common";
import * as CollectionApi from "@/api/collections";
import * as BookmarkApi from "@/api/bookmarks";
import { DropResult } from "@/types/dnd";

type LoadKey = "collectionTree" | "bookmarks";
type MutateKey =
  | "createCollection"
  | "updateCollection"
  | "updateCollectionEmoji"
  | "deleteCollection"
  | "moveCollection"
  | "reorderCollection"
  | "collectionDnd"
  | "createBookmark"
  | "updateBookmark"
  | "deleteBookmark"
  | "moveBookmark";
type ErrorMap<K extends string> = Record<K, string | null>;

interface WorkspaceState {
  collectionNodes: CollectionNode[];
  bookmarks: Bookmark[];
  selectedCollectionId: ID | null;
  isLoading: Record<LoadKey, boolean>;
  error: Record<LoadKey, string | null>;
  isMutating: Record<MutateKey, boolean>;
  mutateError: Record<MutateKey, string | null>;
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

export const useWorkspaceStore = defineStore("workspace", {
  state: (): WorkspaceState => ({
    collectionNodes: [],
    bookmarks: [],
    selectedCollectionId: null,
    isLoading: { collectionTree: false, bookmarks: false },
    error: { collectionTree: null, bookmarks: null },
    isMutating: {
      createCollection: false,
      updateCollection: false,
      updateCollectionEmoji: false,
      deleteCollection: false,
      moveCollection: false,
      reorderCollection: false,
      collectionDnd: false,
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
      collectionDnd: null,
      createBookmark: null,
      updateBookmark: null,
      deleteBookmark: null,
      moveBookmark: null,
    },
  }),

  getters: {
    collectionById(state): Record<ID, CollectionNode> {
      const map = {} as Record<ID, CollectionNode>;
      for (const node of state.collectionNodes) map[node.id] = node;
      return map;
    },
    childrenByParent(state): Record<string, ID[]> {
      const result = {} as Record<string, ID[]>;
      const byId = this.collectionById;

      for (const node of state.collectionNodes) {
        const key = node.parentId == null ? "root" : String(node.parentId);
        (result[key] ??= []).push(node.id);
      }

      // sortOrder 기준으로 정렬
      for (const key in result) {
        result[key].sort((a, b) => byId[a].sortOrder - byId[b].sortOrder);
      }

      return result;
    },
    selectedCollectionNode(): CollectionNode | null {
      const id = this.selectedCollectionId;
      if (id == null) return null;
      return this.collectionById[id] ?? null;
    },
  },

  actions: {
    // 초기화
    resetAll() {
      this.collectionNodes = [];
      this.bookmarks = [];
      this.selectedCollectionId = null;
      this.isLoading = { collectionTree: false, bookmarks: false };
      this.error = { collectionTree: null, bookmarks: null };
      this.isMutating = {
        createCollection: false,
        updateCollection: false,
        updateCollectionEmoji: false,
        deleteCollection: false,
        moveCollection: false,
        reorderCollection: false,
        collectionDnd: false,
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
        collectionDnd: null,
        createBookmark: null,
        updateBookmark: null,
        deleteBookmark: null,
        moveBookmark: null,
      };
    },
    selectCollection(id: ID | null) {
      this.selectedCollectionId = id;
    },
    updateBookmarkCount(collectionId: ID, cnt: number) {
      if (!this.collectionNodes?.length) return;
      this.collectionNodes = this.collectionNodes.map((n) =>
        n.id === collectionId
          ? { ...n, bookmarkCount: Math.max(0, (n.bookmarkCount ?? 0) + cnt) }
          : n
      );
    },
    setBookmarkCount(collectionId: ID, count: number) {
      if (!this.collectionNodes.length) return;
      this.collectionNodes = this.collectionNodes.map((n) =>
        n.id === collectionId ? { ...n, bookmarkCount: Math.max(0, count) } : n
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
        await CollectionApi.createCollection(payload);
        await this.fetchCollectionTree();
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

    async updateCollection(id: ID, payload: { name?: string }) {
      this.mutateError.updateCollection = null;
      setMutating(this.isMutating, "updateCollection", true);
      try {
        await CollectionApi.updateCollection(id, payload);
        await this.fetchCollectionTree();
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
        await CollectionApi.updateCollectionEmoji(id, payload);
        await this.fetchCollectionTree();
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

        // 선택된 컬렉션을 제거한 경우
        if (this.selectedCollectionId === id) {
          this.selectedCollectionId = null;
          this.bookmarks = [];
        }

        await this.fetchCollectionTree();
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

    async moveCollection(id: ID, targetParentId: ID | null) {
      this.mutateError.moveCollection = null;
      setMutating(this.isMutating, "moveCollection", true);
      try {
        await CollectionApi.moveCollection(id, { targetParentId });
        await this.fetchCollectionTree();
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

    async reorderCollection(id: ID, targetIndex: number) {
      this.mutateError.reorderCollection = null;
      setMutating(this.isMutating, "reorderCollection", true);
      try {
        await CollectionApi.reorderCollection(id, { targetIndex });
        await this.fetchCollectionTree();
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

    // 서버 API 반영
    async applyDropResult(result: DropResult) {
      if (this.isMutating.moveCollection || this.isMutating.reorderCollection)
        return;

      if (result.type === "move") {
        await this.moveCollection(result.id, result.targetParentId);
        return;
      }

      // reorder
      const node = this.collectionById[result.id];
      if (!node) return;

      const nodeParentId = node.parentId ?? null;
      if (nodeParentId !== result.parentId) return;

      if (!Number.isInteger(result.targetIndex) || result.targetIndex < 0)
        return;

      await this.reorderCollection(result.id, result.targetIndex);
    },

    // 로컬 트리에 먼저 반영, 실패 시 롤백
    async applyDropResultWithRollback(result: DropResult) {
      if (this.isMutating.collectionDnd) return;
      this.mutateError.collectionDnd = null;
      setMutating(this.isMutating, "collectionDnd", true);

      const rollback = this.applyDropResultLocal(result);

      try {
        if (result.type === "move") {
          await CollectionApi.moveCollection(result.id, {
            targetParentId: result.targetParentId,
          });
          return;
        }

        // reorder
        const node = this.collectionById[result.id];
        if (!node) return;

        const nodeParentId = node.parentId ?? null;
        if (nodeParentId !== result.parentId) return;

        if (!Number.isInteger(result.targetIndex) || result.targetIndex < 0)
          return;

        await CollectionApi.reorderCollection(result.id, {
          targetIndex: result.targetIndex,
        });
      } catch (e) {
        console.error("[collectionDnd] move/reorder failed:", e);
        rollback();
        throw e;
      } finally {
        setMutating(this.isMutating, "collectionDnd", false);
      }
    },

    // 로컬 트리 반영
    applyDropResultLocal(result: DropResult): () => void {
      const prevNodes = JSON.parse(
        JSON.stringify(this.collectionNodes)
      ) as CollectionNode[];

      const rollback = () => {
        this.collectionNodes = prevNodes;
      };

      if (result.type === "move") {
        this.applyMoveLocal(result.id, result.targetParentId);
      } else if (result.type === "reorder") {
        this.applyReorderLocal(result.id, result.parentId, result.targetIndex);
      }

      return rollback;
    },

    parentKey(parentId: ID | null) {
      return parentId == null ? "root" : String(parentId);
    },

    applyMoveLocal(id: ID, targetParentId: ID | null) {
      const idx = this.collectionNodes.findIndex((n) => n.id === id);
      if (idx < 0) return;

      const node = this.collectionNodes[idx];
      const nodeParentId = node.parentId ?? null;

      if (nodeParentId === targetParentId) return;

      const maxOrder = this.collectionNodes
        .filter((n) => (n.parentId ?? null) === targetParentId && n.id !== id)
        .reduce((max, n) => Math.max(max, n.sortOrder ?? 0), -1);

      this.collectionNodes.splice(idx, 1, {
        ...node,
        parentId: targetParentId,
        sortOrder: maxOrder + 1,
      });
    },

    applyReorderLocal(id: ID, parentId: ID | null, targetIndex: number) {
      if (!Number.isInteger(targetIndex) || targetIndex < 0) return;

      const siblings = this.collectionNodes
        .filter((n) => (n.parentId ?? null) === parentId)
        .sort((a, b) => a.sortOrder - b.sortOrder);

      const nodeIndex = siblings.findIndex((n) => n.id === id);
      if (nodeIndex < 0) return;

      const [moved] = siblings.splice(nodeIndex, 1);

      const nextIndex = Math.max(0, Math.min(targetIndex, siblings.length));
      siblings.splice(nextIndex, 0, moved);

      // sortOrder 정리?
      const nextOrderById = new Map<ID, number>();
      siblings.forEach((n, i) => nextOrderById.set(n.id, i));

      this.collectionNodes = this.collectionNodes.map((n) => {
        if ((n.parentId ?? null) !== parentId) return n;
        const nextOrder = nextOrderById.get(n.id);
        if (nextOrder == null) return n;
        return { ...n, sortOrder: nextOrder };
      });
    },

    async fetchCollectionTree() {
      this.error.collectionTree = null;
      setLoading(this.isLoading, "collectionTree", true);

      try {
        const nodes = await CollectionApi.listTree();
        this.collectionNodes = nodes;
      } catch (e) {
        fail(
          this.error,
          "collectionTree",
          e,
          "컬렉션 트리를 불러오지 못했습니다."
        );
        throw e;
      } finally {
        setLoading(this.isLoading, "collectionTree", false);
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
        const list = await BookmarkApi.listBookmarks(cid);
        this.bookmarks = list;

        this.setBookmarkCount(cid, list.length);
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
