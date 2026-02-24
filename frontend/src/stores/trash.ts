import { defineStore } from "pinia";
import type { ID, TrashItem, TrashType } from "@/types/common";
import type { SliceMeta } from "@/api/common";
import * as TrashApi from "@/api/trash";

type MutateKey = "restore" | "delete" | "empty";

interface TrashState {
  loaded: boolean;

  type: TrashType | null;
  page: number;
  size: number;

  items: TrashItem[];
  meta: SliceMeta | null;

  isLoading: boolean;
  isMutating: Record<MutateKey, boolean>;
  error: unknown | null;
}

export const useTrashStore = defineStore("trash", {
  state: (): TrashState => ({
    loaded: false,
    type: null,
    page: 0,
    size: 20,
    items: [],
    meta: null,
    isLoading: false,
    isMutating: {
      restore: false,
      delete: false,
      empty: false,
    },
    error: null,
  }),

  getters: {
    hasNext: (s) => s.meta?.hasNext ?? false,
    totalCount: (s) => s.items.length,
    filterType: (s) => s.type,
  },

  actions: {
    resetAll() {
      this.loaded = false;
      this.type = null;
      this.page = 0;
      this.size = 20;
      this.items = [];
      this.meta = null;
      this.isLoading = false;
      this.isMutating = {
        restore: false,
        delete: false,
        empty: false,
      };
      this.error = null;
    },

    resetQuery() {
      this.type = null;
      this.page = 0;
      this.size = 20;

      this.items = [];
      this.meta = null;

      this.loaded = false;
    },

    setQuery(params: Partial<Pick<TrashState, "type" | "page" | "size">>) {
      const prevType = this.type;
      const prevSize = this.size;

      if (params.type !== undefined) this.type = params.type;
      if (params.page !== undefined) this.page = params.page;
      if (params.size !== undefined) this.size = params.size;

      const queryChanged = this.type !== prevType || this.size !== prevSize;

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
        const res = await TrashApi.getTrash({
          type: this.type ?? undefined,
          page: this.page,
          size: this.size,
        });

        if (this.page === 0) {
          this.items = res.items;
        } else {
          const existingKeys = new Set(
            this.items.map((i) => `${i.type}:${i.id}`),
          );
          const toAppend = res.items.filter(
            (i) => !existingKeys.has(`${i.type}:${i.id}`),
          );
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
        this.page = 0;
        this.loaded = false;

        await this.load(true, { silent: true });
      } catch {
        // mutation 이후 목록 갱신 실패는 UI를 깨지 않기 위해 무시
      }
    },

    async restore(type: TrashType, id: ID) {
      this.isMutating.restore = true;
      try {
        await TrashApi.restoreTrashItem(type, id);

        this.items = this.items.filter(
          (i) => !(i.type === type && i.id === id),
        );

        await this.safeReload();
      } catch (e) {
        throw e;
      } finally {
        this.isMutating.restore = false;
      }
    },

    async delete(type: TrashType, id: ID) {
      this.isMutating.delete = true;
      try {
        await TrashApi.deleteTrashItem(type, id);

        this.items = this.items.filter(
          (i) => !(i.type === type && i.id === id),
        );

        await this.safeReload();
      } catch (e) {
        throw e;
      } finally {
        this.isMutating.delete = false;
      }
    },

    async restoreBulk(type: TrashType, ids: ID[]) {
      if (!ids?.length) return;

      this.isMutating.restore = true;
      try {
        await TrashApi.restoreTrashBulk(type, { ids });

        const idSet = new Set(ids);
        this.items = this.items.filter(
          (i) => !(i.type === type && idSet.has(i.id)),
        );

        await this.safeReload();
      } catch (e) {
        throw e;
      } finally {
        this.isMutating.restore = false;
      }
    },

    async deleteBulk(type: TrashType, ids: ID[]) {
      if (!ids?.length) return;

      this.isMutating.delete = true;
      try {
        await TrashApi.deleteTrashBulk(type, { ids });

        const idSet = new Set(ids);
        this.items = this.items.filter(
          (i) => !(i.type === type && idSet.has(i.id)),
        );

        await this.safeReload();
      } catch (e) {
        throw e;
      } finally {
        this.isMutating.delete = false;
      }
    },

    async empty(type?: TrashType | null) {
      this.isMutating.empty = true;
      try {
        await TrashApi.emptyTrash(type ?? undefined);

        await this.safeReload();
      } catch (e) {
        throw e;
      } finally {
        this.isMutating.empty = false;
      }
    },
  },
});
