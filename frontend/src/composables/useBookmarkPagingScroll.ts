import { computed, nextTick, onUnmounted, Ref } from "vue";
import { useWorkspaceStore } from "@/stores/workspace";
import { useInfiniteScroll } from "./useInfiniteScroll";
import type { ID } from "@/types/common";

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

  const rootMargin = options.rootMargin ?? "200px";
  const threshold = options.threshold ?? 0;

  const isLoadingBookmarks = computed(() => workspace.isLoading.bookmarks);

  const enabled = computed(() =>
    typeof options.enabled === "boolean"
      ? options.enabled
      : (options.enabled?.value ?? true),
  );

  const canLoadMore = computed(
    () =>
      enabled.value && workspace.bookmarksHasNext && !isLoadingBookmarks.value,
  );
  const isLoadingMore = computed(
    () =>
      enabled.value &&
      isLoadingBookmarks.value &&
      workspace.bookmarks.length > 0,
  );
  const isEndReached = computed(
    () =>
      enabled.value && workspace.bookmarksLoaded && !workspace.bookmarksHasNext,
  );

  async function loadMore() {
    if (!canLoadMore.value) return;

    const prevPage = workspace.bookmarksPage;

    const moved = workspace.nextBookmarksPage();
    if (!moved) return;

    try {
      const cid = options.collectionId?.value ?? null;

      await workspace.fetchBookmarks(cid ?? undefined, { append: true });
    } catch {
      workspace.setBookmarksQuery({
        bookmarksPage: prevPage,
        bookmarksLoaded: true,
      });
    }
  }

  const { setup, cleanup } = useInfiniteScroll(
    listWrapRef,
    sentinelRef,
    loadMore,
    { rootMargin, threshold, enabled },
  );

  async function reconnect() {
    cleanup();
    await nextTick();
    setup();
  }

  return {
    canLoadMore,
    isLoadingMore,
    isEndReached,
    loadMore,
    setup,
    cleanup,
    reconnect,
  };
}
