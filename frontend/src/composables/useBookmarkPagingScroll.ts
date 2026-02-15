import { computed, Ref } from "vue";
import { useWorkspaceStore } from "@/stores/workspace";
import type { ID } from "@/types/common";
import { usePagingScrollBase } from "./usePagingScrollBase";

type Options = {
  collectionId?: Ref<ID | null>;
  enabled?: Ref<boolean> | boolean; // 컬렉션뷰: hasSelection, 즐겨찾기뷰: true
  rootMargin?: string;
  threshold?: number;
};

export function useBookmarkPagingScroll(
  listWrapRef: Ref<HTMLElement | null>,
  sentinelRef: Ref<HTMLElement | null>,
  options: Options = {},
) {
  const workspace = useWorkspaceStore();

  const itemCount = computed(() => workspace.bookmarks.length);
  const isLoading = computed(() => workspace.isLoading.bookmarks);
  const hasNext = computed(() => workspace.bookmarksHasNext);
  const loaded = computed(() => workspace.bookmarksLoaded);

  return usePagingScrollBase({
    listWrapRef,
    sentinelRef,

    itemCount,
    isLoading,
    hasNext,
    loaded,

    nextPage: () => workspace.nextBookmarksPage(),

    fetchNext: async () => {
      const cid = options.collectionId?.value ?? null;
      await workspace.fetchBookmarks(cid ?? undefined, { append: true });
    },

    rollbackPage: () => {
      workspace.setBookmarksQuery({
        bookmarksPage: workspace.bookmarksPage - 1,
        bookmarksLoaded: true,
      });
    },

    options,
  });
}
