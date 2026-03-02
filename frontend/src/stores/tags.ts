import { defineStore } from "pinia";
import type { ID, Tag } from "@/types/common";
import type { GetTagsParams, TagSort } from "@/api/tags";
import type { SliceMeta } from "@/api/common";
import * as TagApi from "@/api/tags";
import type { TagSummaryRes } from "@/api/types";

type MutateKey = "create" | "rename" | "merge" | "delete";

interface TagsState {
  loaded: boolean;

  q: string;
  sort: TagSort;
  page: number;
  size: number;

  items: Tag[];
  meta: SliceMeta | null;

  summary: TagSummaryRes | null;

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
    summary: null,
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
    totalTagsCount: (s) => s.items.length,
    hasNext: (s) => s.meta?.hasNext ?? false,
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

    resetPagingForReload() {
      this.page = 0;
      this.items = [];
      this.meta = null;
      this.loaded = false;
    },

    setQuery(params: Partial<Pick<TagsState, "q" | "sort" | "page" | "size">>) {
      const prevQ = this.q;
      const prevSort = this.sort;
      const prevSize = this.size;

      if (params.q !== undefined) this.q = params.q;
      if (params.sort !== undefined) this.sort = params.sort;
      if (params.page !== undefined) this.page = params.page;
      if (params.size !== undefined) this.size = params.size;

      const queryChanged =
        this.q !== prevQ || this.sort !== prevSort || this.size !== prevSize;

      if (queryChanged) {
        this.page = 0;
        this.items = [];
        this.meta = null;
      }

      this.loaded = false;
    },

    nextPage(): boolean {
      if (!this.meta) return false;
      if (!this.meta.hasNext) return false;

      this.page += 1;
      this.loaded = false;
      return true;
    },

    async load(force = false, opts?: { silent?: boolean }) {
      if (this.loaded && !force) return;

      if (!opts?.silent) this.error = null;

      this.isLoading = true;
      try {
        const params: GetTagsParams = {
          q: this.q.trim() ? this.q.trim() : undefined,
          sort: this.sort,
          page: this.page,
          size: this.size,
        };

        const res = await TagApi.getTags(params);

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

    async create(payload: TagApi.TagCreateReq) {
      this.isMutating.create = true;
      try {
        await TagApi.createTag(payload);

        this.resetPagingForReload();
        await this.load(true);

        await this.loadSummary();
      } catch (e) {
        throw e;
      } finally {
        this.isMutating.create = false;
      }
    },

    async rename(id: ID, payload: TagApi.TagUpdateReq) {
      this.isMutating.rename = true;
      try {
        const updated = await TagApi.renameTag(id, payload);

        const exists = this.items.some((t) => t.id === id);

        // 목록에 있으면 즉시 반영
        if (exists) {
          this.items = this.items.map((t) => (t.id === id ? updated : t));
        }

        // 이름순이거나 검색 중이면 리로드
        const needResort = this.sort === "NAME_ASC";
        const searching = this.q.trim().length > 0;

        if (needResort || searching || !exists) {
          this.resetPagingForReload();
          await this.load(true);
        }
      } catch (e) {
        throw e;
      } finally {
        this.isMutating.rename = false;
      }
    },

    async merge(id: ID, payload: TagApi.TagMergeReq) {
      this.isMutating.merge = true;
      try {
        await TagApi.mergeTag(id, payload);

        this.resetPagingForReload();
        await this.load(true);

        await this.loadSummary();
      } catch (e) {
        throw e;
      } finally {
        this.isMutating.merge = false;
      }
    },

    async delete(id: ID) {
      this.isMutating.delete = true;
      try {
        await TagApi.deleteTag(id);

        this.resetPagingForReload();
        await this.load(true);

        await this.loadSummary();
      } catch (e) {
        throw e;
      } finally {
        this.isMutating.delete = false;
      }
    },

    async loadSummary() {
      try {
        this.summary = await TagApi.getTagSummary();
      } catch (e) {
        // 조용히 무시
      }
    },
  },
});
