import { defineStore } from "pinia";
import type { ID, Bookmark, TaggedBookmark } from "@/types/common";
import type { PageMeta } from "@/api/common";
import * as TagApi from "@/api/tags";

type MutateKey = "toggleFavorite";

interface TaggedBookmarksState {
  loaded: boolean;

  tagId: ID | null;

  page: number;
  size: number;

  items: TaggedBookmark[];
  meta: PageMeta | null;

  isLoading: boolean;
  isMutating: Record<MutateKey, boolean>;
  error: unknown | null;
}

export const useTaggedBookmarksStore = defineStore("taggedBookmarks", {
  state: (): TaggedBookmarksState => ({
    loaded: false,
    tagId: null,
    page: 0,
    size: 20,
    items: [],
    meta: null,
    isLoading: false,
    isMutating: { toggleFavorite: false },
    error: null,
  }),

  getters: {
    hasSelection: (s) => s.tagId != null,
    totalCount: (s) => s.meta?.totalElements ?? s.items.length,
    hasNext: (s) => (s.meta ? s.page + 1 < s.meta.totalPages : false),
  },

  actions: {
    resetAll() {
      this.loaded = false;
      this.tagId = null;
      this.page = 0;
      this.size = 20;
      this.items = [];
      this.meta = null;
      this.isLoading = false;
      this.isMutating = { toggleFavorite: false };
      this.error = null;
    },

    setContext(tagId: ID | null) {
      if (this.tagId === tagId) return;
      this.tagId = tagId;
      this.page = 0;
      this.loaded = false;
      this.items = [];
      this.meta = null;
    },

    setQuery(params: Partial<Pick<TaggedBookmarksState, "page" | "size">>) {
      if (params.page !== undefined) this.page = params.page;
      if (params.size !== undefined) this.size = params.size;
      this.loaded = false;
    },

    async load(force = false, opts?: { silent?: boolean }) {
      if (!this.tagId) return;
      if (this.loaded && !force) return;

      if (!opts?.silent) this.error = null;

      if (this.page === 0) {
        this.items = [];
        this.meta = null;
      }

      this.isLoading = true;
      try {
        // TODO: 백엔드에 tagged bookmarks API 추가 후 활성화
        // const res = await TagApi.getTaggedBookmarks(this.tagId, {
        //   page: this.page,
        //   size: this.size,
        // });

        this.items = [];
        this.meta = null;
        this.loaded = true;
      } catch (e) {
        if (!opts?.silent) this.error = e;
        throw e;
      } finally {
        this.isLoading = false;
      }
    },

    async reload() {
      await this.load(true, { silent: true });
    },

    async safeReload() {
      try {
        await this.load(true);
      } catch {
        // mutation 이후 목록 갱신 실패는 UI를 깨지 않기 위해 무시
      }
    },
  },
});
