import { defineStore } from "pinia";
import type { ID, TaggedBookmark } from "@/types/common";
import type { PageMeta } from "@/api/common";
import * as TagApi from "@/api/tags";

type MutateKey = "toggleFavorite" | "detach" | "replace";

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
    isMutating: { toggleFavorite: false, detach: false, replace: false },
    error: null,
  }),

  getters: {
    hasSelection: (s) => s.tagId != null,
    totalCount: (s) => s.meta?.totalElements ?? s.items.length,
    hasNext: (s) => (s.meta ? s.page + 1 < s.meta.totalPages : false),
    isEmpty: (s) => s.loaded && s.items.length === 0,
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
      this.isMutating = {
        toggleFavorite: false,
        detach: false,
        replace: false,
      };
      this.error = null;
    },

    setContext(tagId: ID | null, opts?: { size?: number }) {
      if (this.tagId === tagId) return;

      this.tagId = tagId;
      this.size = opts?.size ?? 20;

      this.page = 0;
      this.loaded = false;
      this.items = [];
      this.meta = null;
      this.error = null;
    },

    setQuery(params: Partial<Pick<TaggedBookmarksState, "page" | "size">>) {
      let changed = false;

      if (params.page !== undefined && this.page !== params.page) {
        this.page = params.page;
        changed = true;
      }
      if (params.size !== undefined && this.size !== params.size) {
        this.size = params.size;
        changed = true;
      }

      if (changed) this.loaded = false;
    },

    nextPage() {
      if (!this.hasNext) return;
      this.page += 1;
      this.loaded = false;
    },

    inavlidate(tagId?: ID) {
      if (!tagId || this.tagId === tagId) {
        this.loaded = false;
      }
    },

    async load(force = false, opts?: { silent?: boolean }) {
      if (!this.tagId) return;
      if (this.loaded && !force) return;
      if (this.isLoading) return;

      if (!opts?.silent) this.error = null;

      this.isLoading = true;
      try {
        const res = await TagApi.getTaggedBookmarks(this.tagId, {
          page: this.page,
          size: this.size,
        });

        if (this.page === 0) {
          this.items = res.items;
        } else {
          const existing = new Set(this.items.map((b) => b.id));
          const appended = res.items.filter((b) => !existing.has(b.id));
          this.items = [...this.items, ...appended];
        }

        this.meta = res.meta;
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

    async detachFromBookmarks(bookmarkIds: ID[], opts?: { reload?: boolean }) {
      if (!this.tagId) return;
      if (!bookmarkIds.length) return;

      this.isMutating.detach = true;
      this.error = null;
      try {
        await TagApi.detachTagFromBookmarks(this.tagId, { bookmarkIds });

        if (opts?.reload === false) return;

        this.page = 0;
        this.loaded = false;
        await this.reload();
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.isMutating.detach = false;
      }
    },

    async replaceOnBookmarks(
      targetTagId: ID,
      bookmarkIds: ID[],
      opts?: { reload?: boolean },
    ) {
      if (!this.tagId) return;
      if (!bookmarkIds.length) return;

      this.isMutating.replace = true;
      this.error = null;
      try {
        await TagApi.replaceTagOnBookmarks(this.tagId, {
          targetTagId,
          bookmarkIds,
        });

        if (opts?.reload === false) return;

        this.page = 0;
        this.loaded = false;
        await this.reload();
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.isMutating.replace = false;
      }
    },

    patchTaggedBookmarkFavorite(id: ID, isFavorite: boolean) {
      const idx = this.items.findIndex((x) => x.id === id);
      if (idx === -1) return;

      const current = this.items[idx];
      this.items = [
        ...this.items.slice(0, idx),
        { ...current, isFavorite },
        ...this.items.slice(idx + 1),
      ];
    },
  },
});
