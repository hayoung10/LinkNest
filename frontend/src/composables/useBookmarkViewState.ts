import { computed, Ref } from "vue";
import { useWorkspaceStore } from "@/stores/workspace";

interface Options {
  enabled?: Ref<boolean> | boolean; // 컬렉션뷰: hasSelection, 즐겨찾기뷰: true
}

export function useBookmarkViewState(options: Options = {}) {
  const workspace = useWorkspaceStore();

  const enabled = computed(() =>
    typeof options.enabled === "boolean"
      ? options.enabled
      : (options.enabled?.value ?? true),
  );

  const isLoadingBookmarks = computed(() => workspace.isLoading.bookmarks);
  const bookmarksError = computed(() => workspace.error.bookmarks);
  const hasError = computed(() => !!bookmarksError.value);

  const isSearching = computed(() => workspace.bookmarksQ.trim().length > 0);

  const isInitialLoading = computed(
    () =>
      enabled.value &&
      isLoadingBookmarks.value &&
      workspace.bookmarks.length === 0 &&
      !isSearching.value,
  );

  const isEmpty = computed(
    () =>
      enabled.value &&
      !isLoadingBookmarks.value &&
      !hasError.value &&
      workspace.bookmarks.length === 0,
  );

  return {
    isLoadingBookmarks,
    bookmarksError,
    hasError,

    isSearching,
    isInitialLoading,
    isEmpty,
  };
}
