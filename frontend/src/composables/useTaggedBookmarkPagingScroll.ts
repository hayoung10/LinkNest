import { computed, Ref } from "vue";
import { useTaggedBookmarksStore } from "@/stores/taggedBookmarks";
import { usePagingScrollBase } from "./usePagingScrollBase";

type Options = {
  enabled?: Ref<boolean> | boolean;
  rootMargin?: string;
  threshold?: number;
};

export function useTaggedBookmarkPagingScroll(
  listWrapRef: Ref<HTMLElement | null>,
  sentinelRef: Ref<HTMLElement | null>,
  options: Options = {},
) {
  const taggedStore = useTaggedBookmarksStore();

  const itemCount = computed(() => taggedStore.items.length);
  const isLoading = computed(() => taggedStore.isLoading);
  const hasNext = computed(() => taggedStore.hasNext);
  const loaded = computed(() => taggedStore.loaded);

  return usePagingScrollBase({
    listWrapRef,
    sentinelRef,

    itemCount,
    isLoading,
    hasNext,
    loaded,

    nextPage: () => taggedStore.nextPage(),

    fetchNext: async () => {
      await taggedStore.load(true);
    },

    rollbackPage: () => {
      taggedStore.setQuery({ page: taggedStore.page - 1 });
    },

    options,
  });
}
