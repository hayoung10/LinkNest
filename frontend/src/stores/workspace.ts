import { defineStore } from "pinia";
import { Bookmark, Collection, ID } from "@/types/common";
import * as CollectionApi from "@/api/collections";

type LoadKey = "collections" | "bookmarks";

interface WorkspaceState {
  collections: Collection[];
  bookmarks: Bookmark[];
  selectedCollectionId: ID | null;
  isLoading: Record<LoadKey, boolean>;
  error: Record<LoadKey, string | null>;
}

function setLoading(
  target: Record<LoadKey, boolean>,
  key: LoadKey,
  isLoading: boolean
) {
  target[key] = isLoading;
}
function fail(
  target: Record<LoadKey, string | null>,
  key: LoadKey,
  e: unknown,
  fallbackMsg: string
) {
  target[key] = e instanceof Error && e.message ? e.message : fallbackMsg;
}

export const useWorkspaceStore = defineStore("workspace", {
  state: (): WorkspaceState => ({
    collections: [],
    bookmarks: [],
    selectedCollectionId: null,
    isLoading: { collections: false, bookmarks: false },
    error: { collections: null, bookmarks: null },
  }),

  getters: {
    getcurrentCollection(state): Collection | null {
      if (state.selectedCollectionId == null) return null;
      return (
        state.collections.find((c) => c.id === state.selectedCollectionId) ??
        null
      );
    },
  },

  actions: {
    // 초기화
    resetAll() {
      this.collections = [];
      this.bookmarks = [];
      this.selectedCollectionId = null;
      this.isLoading = { collections: false, bookmarks: false };
      this.error = { collections: null, bookmarks: null };
    },
    selectCollection(id: ID) {
      this.selectedCollectionId = id;
    },

    // 컬렉션
    async createCollection(payload: {
      name: string;
      icon?: string | null;
      parentId?: ID | null;
    }) {
      setLoading(this.isLoading, "collections", true);
      try {
        const created = await CollectionApi.createCollection(payload);
        if (
          (payload.parentId ?? null) === (this.selectedCollectionId ?? null)
        ) {
          this.collections.push(created);
        }
      } catch (e) {
        fail(this.error, "collections", e, "컬렉션 생성에 실패했습니다.");
      } finally {
        setLoading(this.isLoading, "collections", false);
      }
    },

    async getCollection(id: ID) {
      setLoading(this.isLoading, "collections", true);
      try {
        const col = await CollectionApi.getCollection(id);
        const idx = this.collections.findIndex((c) => c.id === id);
        if (idx >= 0) this.collections.splice(idx, 1, col); // 목록에 있다면 최신 상태로 교체
      } catch (e) {
        fail(
          this.error,
          "collections",
          e,
          "컬렉션 정보를 불러오지 못했습니다."
        );
      } finally {
        setLoading(this.isLoading, "collections", false);
      }
    },

    async updateCollection(
      id: ID,
      payload: { name?: string; icon?: string | null }
    ) {
      setLoading(this.isLoading, "collections", true);
      try {
        const updated = await CollectionApi.updateCollection(id, payload);
        const idx = this.collections.findIndex((c) => c.id === id);
        if (idx >= 0) this.collections.splice(idx, 1, updated);
      } catch (e) {
        fail(this.error, "collections", e, "컬렉션 수정에 실패했습니다.");
      } finally {
        setLoading(this.isLoading, "collections", false);
      }
    },

    async deleteCollection(id: ID) {
      setLoading(this.isLoading, "collections", true);
      try {
        await CollectionApi.deleteCollection(id);
        this.collections = this.collections.filter((c) => c.id !== id);
        if (this.selectedCollectionId === id) this.selectedCollectionId = null; // 선택된 컬렉션을 제거한 경우
      } catch (e) {
        fail(this.error, "collections", e, "컬렉션 삭제에 실패했습니다.");
      } finally {
        setLoading(this.isLoading, "collections", false);
      }
    },

    async fetchCollections(parentId?: ID | null) {
      setLoading(this.isLoading, "collections", true);
      try {
        const pid = parentId ?? this.selectedCollectionId ?? null;
        this.collections = await CollectionApi.listChildren(pid);
      } catch (e) {
        fail(
          this.error,
          "collections",
          e,
          "컬렉션 목록을 불러오지 못했습니다."
        );
      } finally {
        setLoading(this.isLoading, "collections", false);
      }
    },

    async moveCollection(id: ID, targetParentId: ID | null) {
      setLoading(this.isLoading, "collections", true);
      try {
        await CollectionApi.moveCollection(id, { targetParentId });
        await this.fetchCollections(this.selectedCollectionId ?? null);
      } catch (e) {
        fail(this.error, "collections", e, "컬렉션 이동에 실패했습니다.");
      } finally {
        setLoading(this.isLoading, "collections", false);
      }
    },

    async reorderCollection(id: ID, newOrder: number) {
      setLoading(this.isLoading, "collections", true);
      try {
        await CollectionApi.reorderCollection(id, { newOrder });
        await this.fetchCollections(this.selectedCollectionId ?? null);
      } catch (e) {
        fail(this.error, "collections", e, "컬렉션 정렬에 실패했습니다.");
      } finally {
        setLoading(this.isLoading, "collections", false);
      }
    },

    // 북마크
    async createBookmark(payload: {
      collectionId: ID;
      url: string;
      title?: string;
      description?: string;
    }) {
      setLoading(this.isLoading, "bookmarks", true);
      try {
        // TODO: API 연결
      } catch (e) {
        fail(this.error, "bookmarks", e, "북마크 생성에 실패했습니다.");
      } finally {
        setLoading(this.isLoading, "bookmarks", false);
      }
    },

    async getBookmark(id: ID) {
      setLoading(this.isLoading, "bookmarks", true);
      try {
        // TODO: API 연결
      } catch (e) {
        fail(this.error, "bookmarks", e, "북마크 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(this.isLoading, "bookmarks", false);
      }
    },

    async updateBookmark(
      id: ID,
      payload: { url?: string; title?: string; description?: string }
    ) {
      setLoading(this.isLoading, "bookmarks", true);
      try {
        // TODO: API 연결
      } catch (e) {
        fail(this.error, "bookmarks", e, "북마크 수정에 실패했습니다.");
      } finally {
        setLoading(this.isLoading, "bookmarks", false);
      }
    },

    async deleteBookmark(id: ID) {
      setLoading(this.isLoading, "bookmarks", true);
      try {
        // TODO: API 연결
      } catch (e) {
        fail(this.error, "bookmarks", e, "북마크 삭제에 실패했습니다.");
      } finally {
        setLoading(this.isLoading, "bookmarks", false);
      }
    },

    async fetchBookmarks(collectionId?: ID) {
      setLoading(this.isLoading, "bookmarks", true);
      try {
        // TODO: API 연결
      } catch (e) {
        fail(this.error, "bookmarks", e, "북마크 목록을 불러오지 못했습니다.");
      } finally {
        setLoading(this.isLoading, "bookmarks", false);
      }
    },

    async moveBookmark(id: ID, targetCollectionId: ID) {
      setLoading(this.isLoading, "bookmarks", true);
      try {
        // TODO: API 연결
      } catch (e) {
        fail(this.error, "bookmarks", e, "북마크 이동에 실패했습니다.");
      } finally {
        setLoading(this.isLoading, "bookmarks", false);
      }
    },
  },
});
