import { onMounted, onUnmounted, ref } from "vue";

type Options = {
  minIntervalMs?: number;
};

export function useTabActivationRefresh(
  refresh: () => void | Promise<void>,
  options: Options = {},
) {
  const minIntervalMs = options.minIntervalMs ?? 500;
  const lastRefreshAt = ref(0);

  function shouldRun() {
    const now = Date.now();
    if (now - lastRefreshAt.value < minIntervalMs) return false;
    lastRefreshAt.value = now;
    return true;
  }

  function runRefresh() {
    if (!shouldRun()) return;
    void refresh();
  }

  function onVisibilitychange() {
    if (document.visibilityState === "visible") {
      runRefresh();
    }
  }

  function onWindowFocus() {
    runRefresh();
  }

  onMounted(() => {
    document.addEventListener("visibilitychange", onVisibilitychange);
    window.addEventListener("focus", onWindowFocus);
  });

  onUnmounted(() => {
    document.removeEventListener("visibilitychange", onVisibilitychange);
    window.removeEventListener("focus", onWindowFocus);
  });

  return {
    runRefresh,
  };
}
