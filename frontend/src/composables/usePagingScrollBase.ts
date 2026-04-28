import { computed, nextTick, Ref } from "vue";
import { useInfiniteScroll } from "./useInfiniteScroll";

type Options = {
  enabled?: Ref<boolean> | boolean;
  rootMargin?: string;
  threshold?: number;
};

type Params = {
  listWrapRef: Ref<HTMLElement | null>;
  sentinelRef: Ref<HTMLElement | null>;

  itemCount: Ref<number>;
  isLoading: Ref<boolean>;
  hasNext: Ref<boolean>;
  loaded: Ref<boolean>;

  nextPage: () => boolean;
  fetchNext: () => Promise<void>;
  rollbackPage?: () => void;

  options?: Options;
};

export function usePagingScrollBase(params: Params) {
  const {
    listWrapRef,
    sentinelRef,
    itemCount,
    isLoading,
    hasNext,
    loaded,
    nextPage,
    fetchNext,
    rollbackPage,
    options = {},
  } = params;

  const rootMargin = options.rootMargin ?? "200px";
  const threshold = options.threshold ?? 0;

  const enabled = computed(() =>
    typeof options.enabled === "boolean"
      ? options.enabled
      : (options.enabled?.value ?? true),
  );

  const canLoadMore = computed(
    () => enabled.value && hasNext.value && !isLoading.value,
  );
  const isLoadingMore = computed(
    () => enabled.value && isLoading.value && itemCount.value > 0,
  );
  const isEndReached = computed(
    () => enabled.value && loaded.value && !hasNext.value,
  );

  async function loadMore() {
    if (!canLoadMore.value) return;

    const moved = nextPage();
    if (!moved) return;

    try {
      await fetchNext();
    } catch {
      rollbackPage?.();
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
