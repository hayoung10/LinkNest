import { onBeforeUnmount, Ref, watch } from "vue";

type Options = {
  rootMargin?: string; // 기본 "200px"
  threshold?: number;
  enabled?: Ref<boolean> | boolean;
};

export function useInfiniteScroll(
  rootRef: Ref<HTMLElement | null>,
  sentinelRef: Ref<HTMLElement | null>,
  onReachEnd: () => void | Promise<void>,
  options: Options = {},
) {
  const rootMargin = options.rootMargin ?? "200px";
  const threshold = options.threshold ?? 0;

  let observer: IntersectionObserver | null = null;

  function cleanup() {
    observer?.disconnect();
    observer = null;
  }

  function setup() {
    cleanup();

    const root = rootRef.value;
    const target = sentinelRef.value;
    if (!root || !target) return;

    const enabled =
      typeof options.enabled === "boolean"
        ? options.enabled
        : (options.enabled?.value ?? true);

    if (!enabled) return;

    observer = new IntersectionObserver(
      (entries) => {
        const entry = entries.find((e) => e.isIntersecting);
        if (!entry) return;

        void onReachEnd();
      },
      { root, rootMargin, threshold },
    );

    observer.observe(target);
  }

  watch([rootRef, sentinelRef], setup, { flush: "post" });

  onBeforeUnmount(cleanup);

  return { setup, cleanup };
}
