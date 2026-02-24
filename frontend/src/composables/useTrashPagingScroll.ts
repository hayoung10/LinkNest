import { computed, Ref } from "vue";
import { useTrashStore } from "@/stores/trash";
import { usePagingScrollBase } from "./usePagingScrollBase";

type Options = {
  enabled?: Ref<boolean> | boolean;
  rootMargin?: string;
  threshold?: number;
};

export function useTrashPagingScroll(
  listWrapRef: Ref<HTMLElement | null>,
  sentinelRef: Ref<HTMLElement | null>,
  options: Options = {},
) {
  const trash = useTrashStore();

  const itemCount = computed(() => trash.items.length);
  const isLoading = computed(() => trash.isLoading);
  const hasNext = computed(() => trash.hasNext);
  const loaded = computed(() => trash.loaded);

  return usePagingScrollBase({
    listWrapRef,
    sentinelRef,

    itemCount,
    isLoading,
    hasNext,
    loaded,

    nextPage: () => trash.nextPage(),

    fetchNext: async () => {
      await trash.load(true);
    },

    rollbackPage: () => {
      trash.setQuery({ page: trash.page - 1 });
    },

    options,
  });
}
