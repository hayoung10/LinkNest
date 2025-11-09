import { defineStore } from "pinia";
import { Bookmark, Collection, ID } from "@/types/common";
import {
  CollectionCreateReq,
  CollectionMoveReq,
  CollectionReorderReq,
  CollectionUpdateReq,
} from "@/api/collections";
import {
  BookmarkCreateReq,
  BookmarkMoveReq,
  BookmarkUpdateReq,
} from "@/api/bookmarks";

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
  isloading: boolean
) {
  target[key] = isloading;
}
function fail(
  target: Record<LoadKey, string | null>,
  key: LoadKey,
  e: unknown,
  fallbackMsg: string
) {
  target[key] = e instanceof Error && e.message ? e.message : fallbackMsg;
}

export const useWorkspaceStore = defineStore<"workspace", WorkspaceState>(
  "workspace",
  {
    state: () => ({
      collections: [],
      bookmarks: [],
      selectedCollectionId: null,
      isLoading: { collections: false, bookmarks: false },
      error: { collections: null, bookmarks: null },
    }),

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
      async createCollection(payload: CollectionCreateReq) {
        setLoading(this.isLoading, "collections", true);
        try {
          // TODO: API 연결
        } catch (e) {
          fail(this.error, "collections", e, "컬렉션 생성에 실패했습니다.");
        } finally {
          setLoading(this.isLoading, "collections", false);
        }
      },

      async getCollection(id: ID) {
        setLoading(this.isLoading, "collections", true);
        try {
          // TODO: API 연결
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

      async updateCollection(id: ID, payload: CollectionUpdateReq) {
        setLoading(this.isLoading, "collections", true);
        try {
          // TODO: API 연결
        } catch (e) {
          fail(this.error, "collections", e, "컬렉션 수정에 실패했습니다.");
        } finally {
          setLoading(this.isLoading, "collections", false);
        }
      },

      async deleteCollection(id: ID) {
        setLoading(this.isLoading, "collections", true);
        try {
          // TODO: API 연결
        } catch (e) {
          fail(this.error, "collections", e, "컬렉션 삭제에 실패했습니다.");
        } finally {
          setLoading(this.isLoading, "collections", false);
        }
      },

      async fetchCollections(parentId: ID | null = null) {
        setLoading(this.isLoading, "collections", true);
        try {
          // TODO: API 연결
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

      async moveCollection(id: ID, payload: CollectionMoveReq) {
        setLoading(this.isLoading, "collections", true);
        try {
          // TODO: API 연결
        } catch (e) {
          fail(this.error, "collections", e, "컬렉션 이동에 실패했습니다.");
        } finally {
          setLoading(this.isLoading, "collections", false);
        }
      },

      async reorderCollection(id: ID, payload: CollectionReorderReq) {
        setLoading(this.isLoading, "collections", true);
        try {
          // TODO: API 연결
        } catch (e) {
          fail(this.error, "collections", e, "컬렉션 정렬에 실패했습니다.");
        } finally {
          setLoading(this.isLoading, "collections", false);
        }
      },

      // 북마크
      async createBookmark(payload: BookmarkCreateReq) {
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
          fail(
            this.error,
            "bookmarks",
            e,
            "북마크 정보를 불러오지 못했습니다."
          );
        } finally {
          setLoading(this.isLoading, "bookmarks", false);
        }
      },

      async updateBookmark(id: ID, payload: BookmarkUpdateReq) {
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
          fail(
            this.error,
            "bookmarks",
            e,
            "북마크 목록을 불러오지 못했습니다."
          );
        } finally {
          setLoading(this.isLoading, "bookmarks", false);
        }
      },

      async moveBookmark(id: ID, payload: BookmarkMoveReq) {
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
  }
);
