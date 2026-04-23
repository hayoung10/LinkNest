import { defineStore } from "pinia";
import type { Bookmark, CollectionNode, ID } from "@/types/common";
import type { SliceMeta } from "@/api/common";
import * as CollectionApi from "@/api/collections";
import * as BookmarkApi from "@/api/bookmarks";
import { DropResult } from "@/types/dnd";
import { refreshBookmarkAutoImage } from "@/utils/refreshAutoImage";
import { toHttpError } from "@/api/errors";

type ViewMode = "collection" | "favorites";
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
  | "moveBookmark"
  | "toggleBookmarkFavorite";
type ErrorMap<K extends string> = Record<K, string | null>;

interface WorkspaceState {
  viewMode: ViewMode;
  collectionNodes: CollectionNode[];
  bookmarks: Bookmark[];
  selectedCollectionId: ID | null;

  bookmarksQ: string;
  bookmarksPage: number;
  bookmarksSize: number;
  bookmarksMeta: SliceMeta | null;
  bookmarksLoaded: boolean;

  isLoading: Record<LoadKey, boolean>;
  error: Record<LoadKey, string | null>;
  isMutating: Record<MutateKey, boolean>;
  mutateError: Record<MutateKey, string | null>;

  expandedIds: ID[];
  _fetchCollectionTreeSeq: number;
  _fetchBookmarksSeq: number;
}

function setLoading(
  target: Record<LoadKey, boolean>,
  key: LoadKey,
  isLoading: boolean,
) {
  target[key] = isLoading;
}
function fail<K extends string>(
  target: ErrorMap<K>,
  key: K,
  e: unknown,
  fallbackMsg: string,
) {
  target[key] = e instanceof Error && e.message ? e.message : fallbackMsg;
}
function setMutating(
  target: Record<MutateKey, boolean>,
  key: MutateKey,
  v: boolean,
) {
  target[key] = v;
}

export const useWorkspaceStore = defineStore("workspace", {
  state: (): WorkspaceState => ({
    viewMode: "collection",
    collectionNodes: [],
    bookmarks: [],
    selectedCollectionId: null,
    bookmarksQ: "",
    bookmarksPage: 0,
    bookmarksSize: 20,
    bookmarksMeta: null,
    bookmarksLoaded: false,
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
      toggleBookmarkFavorite: false,
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
      toggleBookmarkFavorite: null,
    },
    expandedIds: [],
    _fetchCollectionTreeSeq: 0,
    _fetchBookmarksSeq: 0,
  }),

  getters: {
    collectionById(state): Record<ID, CollectionNode> {
      const map = {} as Record<ID, CollectionNode>;
      for (const node of state.collectionNodes) map[node.id] = node;
      return map;
    },
    collectionInfoById(
      state,
    ): Record<ID, { name: string; emoji: string | null; parentId: ID | null }> {
      const map = {} as Record<
        ID,
        { name: string; emoji: string | null; parentId: ID | null }
      >;

      for (const n of state.collectionNodes) {
        map[n.id] = { name: n.name, emoji: n.emoji, parentId: n.parentId };
      }

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
    isFavoriteView(): boolean {
      return this.viewMode === "favorites";
    },
    expandedSet(state): Set<ID> {
      return new Set(state.expandedIds);
    },
    bookmarksTotalCount(state): number {
      return state.bookmarks.length;
    },
    bookmarksHasNext(state): boolean {
      return state.bookmarksMeta?.hasNext ?? false;
    },
  },

  actions: {
    // 초기화
    resetAll() {
      this.viewMode = "collection";
      this.collectionNodes = [];
      this.bookmarks = [];
      this.selectedCollectionId = null;
      this.bookmarksQ = "";
      this.bookmarksPage = 0;
      this.bookmarksSize = 20;
      this.bookmarksMeta = null;
      this.bookmarksLoaded = false;
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
        toggleBookmarkFavorite: false,
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
        toggleBookmarkFavorite: null,
      };
      this.expandedIds = [];
      this._fetchCollectionTreeSeq = 0;
      this._fetchBookmarksSeq = 0;
    },
    selectCollection(id: ID | null) {
      this.viewMode = "collection";
      this.selectedCollectionId = id;
      this.resetBookmarksQuery();

      if (id != null) {
        this.expandAncestors(id);
      }
    },
    selectFavorites() {
      this.viewMode = "favorites";
      this.selectedCollectionId = null;
      this.resetBookmarksQuery();
    },
    updateBookmarkCount(collectionId: ID, cnt: number) {
      if (!this.collectionNodes?.length) return;
      this.collectionNodes = this.collectionNodes.map((n) =>
        n.id === collectionId
          ? { ...n, bookmarkCount: Math.max(0, (n.bookmarkCount ?? 0) + cnt) }
          : n,
      );
    },
    setBookmarkCount(collectionId: ID, count: number) {
      if (!this.collectionNodes.length) return;
      this.collectionNodes = this.collectionNodes.map((n) =>
        n.id === collectionId ? { ...n, bookmarkCount: Math.max(0, count) } : n,
      );
    },

    /* ------------------------ 트리 상태 ------------------------ */
    toggleExpanded(id: ID) {
      const set = new Set(this.expandedIds);
      set.has(id) ? set.delete(id) : set.add(id);
      this.expandedIds = Array.from(set);
    },
    setExpanded(id: ID, expanded: boolean) {
      const set = new Set(this.expandedIds);
      expanded ? set.add(id) : set.delete(id);
      this.expandedIds = Array.from(set);
    },
    collapseAll() {
      this.expandedIds = [];
    },
    syncExpandedWithTree(validIds: Set<ID>) {
      const next = this.expandedIds.filter((id) => validIds.has(id));
      if (next.length !== this.expandedIds.length) {
        this.expandedIds = next;
      }
    },
    expandAncestors(collectionId: ID) {
      const byId = this.collectionById;
      const set = new Set(this.expandedIds);

      let cur = byId[collectionId];
      while (cur.parentId != null) {
        set.add(cur.parentId);
        cur = byId[cur.parentId];
      }

      this.expandedIds = Array.from(set);
    },

    /* ------------------------ 페이징 ------------------------ */
    resetBookmarksPage() {
      this.bookmarksPage = 0;
      this.bookmarksMeta = null;
      this.bookmarksLoaded = false;
    },
    nextBookmarksPage(): boolean {
      if (!this.bookmarksMeta) return false;
      if (!this.bookmarksHasNext) return false;
      this.bookmarksPage += 1;
      this.bookmarksLoaded = false;
      return true;
    },
    async reloadBookmarks(collectionId?: ID) {
      this.resetBookmarksPage();
      await this.fetchBookmarks(collectionId);
    },
    resetBookmarksQuery() {
      this.bookmarksQ = "";
      this.resetBookmarksPage();
    },
    setBookmarksQuery(
      params: Partial<
        Pick<
          WorkspaceState,
          "bookmarksQ" | "bookmarksPage" | "bookmarksSize" | "bookmarksLoaded"
        >
      >,
    ) {
      const prevQ = this.bookmarksQ;
      const prevSize = this.bookmarksSize;

      if (params.bookmarksQ !== undefined) this.bookmarksQ = params.bookmarksQ;
      if (params.bookmarksPage !== undefined)
        this.bookmarksPage = params.bookmarksPage;
      if (params.bookmarksSize !== undefined)
        this.bookmarksSize = params.bookmarksSize;
      if (params.bookmarksLoaded !== undefined)
        this.bookmarksLoaded = params.bookmarksLoaded;

      const queryChanged =
        this.bookmarksQ !== prevQ || this.bookmarksSize !== prevSize;

      if (queryChanged) {
        this.bookmarksPage = 0;
        this.bookmarks = [];
        this.bookmarksMeta = null;
      }

      this.bookmarksLoaded = false;
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
          "컬렉션 생성에 실패했습니다.",
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
          "컬렉션 수정에 실패했습니다.",
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
          "컬렉션 이모지 수정에 실패했습니다.",
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
          "컬렉션 삭제에 실패했습니다.",
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "deleteCollection", false);
      }
    },

    async moveCollection(
      id: ID,
      targetParentId: ID | null,
      opts?: { refreshTree?: boolean },
    ) {
      const refreshTree = opts?.refreshTree ?? true;

      this.mutateError.moveCollection = null;
      setMutating(this.isMutating, "moveCollection", true);
      try {
        await CollectionApi.moveCollection(id, { targetParentId });

        if (refreshTree) {
          await this.fetchCollectionTree();
        }
      } catch (e) {
        fail(
          this.mutateError,
          "moveCollection",
          e,
          "컬렉션 이동에 실패했습니다.",
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "moveCollection", false);
      }
    },

    async reorderCollection(
      id: ID,
      targetIndex: number,
      opts?: { refreshTree?: boolean },
    ) {
      const refreshTree = opts?.refreshTree ?? true;

      this.mutateError.reorderCollection = null;
      setMutating(this.isMutating, "reorderCollection", true);
      try {
        await CollectionApi.reorderCollection(id, { targetIndex });

        if (refreshTree) {
          await this.fetchCollectionTree();
        }
      } catch (e) {
        fail(
          this.mutateError,
          "reorderCollection",
          e,
          "컬렉션 정렬에 실패했습니다.",
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "reorderCollection", false);
      }
    },

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
        const err = toHttpError(e);
        console.error("[collectionDnd] move/reorder failed", {
          message: err.message,
          status: err.status,
          type: err.type,
        });

        rollback();
        throw e;
      } finally {
        setMutating(this.isMutating, "collectionDnd", false);
      }
    },

    // 로컬 트리 반영
    applyDropResultLocal(result: DropResult): () => void {
      const prevNodes = JSON.parse(
        JSON.stringify(this.collectionNodes),
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
      const seq = ++this._fetchCollectionTreeSeq;

      this.error.collectionTree = null;
      setLoading(this.isLoading, "collectionTree", true);

      try {
        const nodes = await CollectionApi.listTree();

        if (seq !== this._fetchCollectionTreeSeq) return;

        this.collectionNodes = nodes;

        const validIds = new Set(nodes.map((n) => n.id));
        this.syncExpandedWithTree(validIds);

        if (this.selectedCollectionId != null) {
          this.expandAncestors(this.selectedCollectionId);
        }
      } catch (e) {
        if (seq !== this._fetchCollectionTreeSeq) return;
        fail(
          this.error,
          "collectionTree",
          e,
          "컬렉션 트리를 불러오지 못했습니다.",
        );
        throw e;
      } finally {
        if (seq === this._fetchCollectionTreeSeq) {
          setLoading(this.isLoading, "collectionTree", false);
        }
      }
    },

    /* ------------------------ 북마크 ------------------------ */
    async createBookmark(payload: BookmarkApi.BookmarkCreateReq) {
      this.mutateError.createBookmark = null;
      setMutating(this.isMutating, "createBookmark", true);
      try {
        const created = await BookmarkApi.createBookmark(payload);
        this.updateBookmarkCount(created.collectionId, +1);

        const needsAutoRefresh =
          created.imageMode === "AUTO" &&
          (!created.autoImageUrl || created.autoImageUrl.trim() === "");

        if (needsAutoRefresh) {
          refreshBookmarkAutoImage(
            created.id,
            BookmarkApi.getBookmark,
            (latest) => this.replaceBookmark(latest),
          );
        }
      } catch (e) {
        fail(
          this.mutateError,
          "createBookmark",
          e,
          "북마크 생성에 실패했습니다.",
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
        this.replaceBookmark(updated);

        const needsAutoRefresh =
          updated.imageMode === "AUTO" &&
          (!updated.autoImageUrl || updated.autoImageUrl.trim() === "");

        if (needsAutoRefresh) {
          refreshBookmarkAutoImage(
            updated.id,
            BookmarkApi.getBookmark,
            (latest) => this.replaceBookmark(latest),
          );
        }
      } catch (e) {
        fail(
          this.mutateError,
          "updateBookmark",
          e,
          "북마크 수정에 실패했습니다.",
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
          "북마크 삭제에 실패했습니다.",
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "deleteBookmark", false);
      }
    },

    async fetchBookmarks(collectionId?: ID, opts?: { append?: boolean }) {
      const seq = ++this._fetchBookmarksSeq;
      const append = !!opts?.append && this.bookmarksPage > 0;

      this.error.bookmarks = null;
      setLoading(this.isLoading, "bookmarks", true);
      try {
        // 즐겨찾기 뷰
        if (this.viewMode === "favorites") {
          const res = await BookmarkApi.listFavorites({
            q: this.bookmarksQ.trim() ? this.bookmarksQ.trim() : undefined,
            page: this.bookmarksPage,
            size: this.bookmarksSize,
          });

          if (seq !== this._fetchBookmarksSeq) return;

          if (!append) {
            this.bookmarks = res.items;
          } else {
            const existing = new Set(this.bookmarks.map((b) => b.id));
            const appended = res.items.filter((b) => !existing.has(b.id));
            this.bookmarks = [...this.bookmarks, ...appended];
          }

          this.bookmarksMeta = res.meta;
          this.bookmarksLoaded = true;

          return;
        }

        // 컬렉션 뷰
        const cid = collectionId ?? this.selectedCollectionId ?? null;
        if (cid == null) {
          this.bookmarks = [];
          this.bookmarksMeta = null;
          this.bookmarksLoaded = false;
          return;
        }
        const res = await BookmarkApi.listBookmarks({
          collectionId: cid,
          q: this.bookmarksQ.trim() ? this.bookmarksQ.trim() : undefined,
          page: this.bookmarksPage,
          size: this.bookmarksSize,
        });

        if (seq !== this._fetchBookmarksSeq) return;

        if (!append) {
          this.bookmarks = res.items;
        } else {
          const existing = new Set(this.bookmarks.map((b) => b.id));
          const appended = res.items.filter((b) => !existing.has(b.id));
          this.bookmarks = [...this.bookmarks, ...appended];
        }

        this.bookmarksMeta = res.meta;
        this.bookmarksLoaded = true;
      } catch (e) {
        if (seq !== this._fetchBookmarksSeq) return;
        fail(this.error, "bookmarks", e, "북마크 목록을 불러오지 못했습니다.");
        throw e;
      } finally {
        if (seq === this._fetchBookmarksSeq) {
          setLoading(this.isLoading, "bookmarks", false);
        }
      }
    },

    async moveBookmark(id: ID, targetCollectionId: ID) {
      this.mutateError.moveBookmark = null;
      setMutating(this.isMutating, "moveBookmark", true);

      const target = this.bookmarks.find((b) => b.id === id);
      const originCollectionId = target?.collectionId ?? null;

      try {
        await BookmarkApi.moveBookmark(id, { targetCollectionId });

        if (
          originCollectionId != null &&
          originCollectionId !== targetCollectionId
        ) {
          this.updateBookmarkCount(originCollectionId, -1);
          this.updateBookmarkCount(targetCollectionId, +1);
        }

        if (this.selectedCollectionId != null) {
          await this.fetchBookmarks(this.selectedCollectionId);
        } else {
          this.bookmarks = [];
          this.resetBookmarksPage();
        }
      } catch (e) {
        fail(
          this.mutateError,
          "moveBookmark",
          e,
          "북마크 이동에 실패했습니다.",
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

    async toggleBookmarkFavorite(bookmark: Bookmark): Promise<Bookmark> {
      this.mutateError.toggleBookmarkFavorite = null;
      setMutating(this.isMutating, "toggleBookmarkFavorite", true);
      try {
        const updated = await BookmarkApi.updateFavorite(bookmark.id, {
          isFavorite: !bookmark.isFavorite,
        });

        if (this.viewMode === "favorites" && !updated.isFavorite) {
          this.bookmarks = this.bookmarks.filter((b) => b.id !== updated.id);
          return updated;
        }

        this.replaceBookmark(updated);
        return updated;
      } catch (e) {
        fail(
          this.mutateError,
          "toggleBookmarkFavorite",
          e,
          "즐겨찾기 변경에 실패했습니다.",
        );
        throw e;
      } finally {
        setMutating(this.isMutating, "toggleBookmarkFavorite", false);
      }
    },
  },
});
