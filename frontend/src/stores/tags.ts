import { defineStore } from "pinia";
import type { ID, Tag } from "@/types/common";
import type { GetTagsParams, TagSort } from "@/api/tags";
import type { PageMeta } from "@/api/common";
import * as TagApi from "@/api/tags";

type MutateKey = "create" | "rename" | "merge" | "delete";

interface TagsState {
  loaded: boolean;

  q: string;
  sort: TagSort;
  page: number;
  size: number;

  items: Tag[];
  meta: PageMeta | null;
  totalBookmarks: number | null;

  isLoading: boolean;
  isMutating: Record<MutateKey, boolean>;
  error: unknown | null;
}

export const useTagsStore = defineStore("tags", {
  state: (): TagsState => ({
    loaded: false,
    q: "",
    sort: "NAME_ASC",
    page: 0,
    size: 20,
    items: [],
    meta: null,
    totalBookmarks: null,
    isLoading: false,
    isMutating: {
      create: false,
      rename: false,
      merge: false,
      delete: false,
    },
    error: null,
  }),

  getters: {
    totalTagsCount: (s) => s.meta?.totalElements ?? s.items.length,
  },

  actions: {
    resetAll() {
      this.loaded = false;
      this.q = "";
      this.sort = "NAME_ASC";
      this.page = 0;
      this.size = 20;
      this.items = [];
      this.meta = null;
      this.totalBookmarks = null;
      this.isLoading = false;
      this.isMutating = {
        create: false,
        rename: false,
        merge: false,
        delete: false,
      };
      this.error = null;
    },

    resetQuery() {
      this.q = "";
      this.sort = "NAME_ASC";
      this.page = 0;
      this.size = 20;
      this.loaded = false;
    },

    setQuery(params: Partial<Pick<TagsState, "q" | "sort" | "page" | "size">>) {
      if (params.q !== undefined) this.q = params.q;
      if (params.sort !== undefined) this.sort = params.sort;
      if (params.page !== undefined) this.page = params.page;
      if (params.size !== undefined) this.size = params.size;

      this.loaded = false;
    },

    async load(force = false) {
      if (this.loaded && !force) return;

      this.error = null;

      if (this.page === 0) {
        this.items = [];
        this.meta = null;
      }

      this.isLoading = true;
      try {
        const params: GetTagsParams = {
          q: this.q.trim() ? this.q.trim() : undefined,
          sort: this.sort,
          page: this.page,
          size: this.size,
        };

        const res = await TagApi.getTags(params);

        this.totalBookmarks = res.totalBookmarks;

        if (this.page === 0) {
          this.items = res.items;
        } else {
          const existingIds = new Set(this.items.map((t) => t.id));
          const toAppend = res.items.filter((t) => !existingIds.has(t.id));
          this.items = [...this.items, ...toAppend];
        }

        this.meta = res.meta;
        this.loaded = true;
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.isLoading = false;
      }
    },

    async reload() {
      await this.load(true);
    },

    async create(payload: TagApi.TagCreateReq) {
      this.error = null;
      this.isMutating.create = true;
      try {
        await TagApi.createTag(payload);

        await this.reload();
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.isMutating.create = false;
      }
    },

    async rename(id: ID, payload: TagApi.TagUpdateReq) {
      this.error = null;
      this.isMutating.rename = true;
      try {
        const updated = await TagApi.renameTag(id, payload);

        const idx = this.items.findIndex((t) => t.id === id);
        if (idx >= 0) this.items[idx] = updated;
        else await this.reload();
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.isMutating.rename = false;
      }
    },

    async merge(id: ID, payload: TagApi.TagMergeReq) {
      this.error = null;
      this.isMutating.merge = true;
      try {
        await TagApi.mergeTag(id, payload);
        await this.reload();
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.isMutating.merge = false;
      }
    },

    async delete(id: ID) {
      this.error = null;
      this.isMutating.delete = true;
      try {
        await TagApi.deleteTag(id);
        await this.reload();
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.isMutating.delete = false;
      }
    },
  },
});
