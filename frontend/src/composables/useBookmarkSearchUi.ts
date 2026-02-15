import { computed, nextTick } from "vue";
import { useWorkspaceStore } from "@/stores/workspace";

interface Options {
  scrollToTop?: () => void;
  afterSearched?: () => void | Promise<void>;
}

export function useBookmarkSearchUi(options: Options = {}) {
  const workspace = useWorkspaceStore();

  const searchQ = computed(() => workspace.bookmarksQ.trim().normalize("NFC"));

  const emptySearchDescription = computed(
    () => `'${searchQ.value}'에 대한 결과를 찾을 수 없습니다.`,
  );

  async function onSearched() {
    await nextTick();
    options.scrollToTop?.();
    await options.afterSearched?.();
  }

  return {
    searchQ,
    emptySearchDescription,
    onSearched,
  };
}
