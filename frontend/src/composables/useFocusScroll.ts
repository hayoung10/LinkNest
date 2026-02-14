import { ComponentPublicInstance, nextTick, ref, Ref, watch } from "vue";
import type { ID } from "@/types/common";

type Options = {
  block?: ScrollLogicalPosition;
  behavior?: ScrollBehavior;
  doneDelayMs?: number;
};

export function useFocusScroll(
  focusIdRef: Ref<ID | null>,
  isLoadingRef: Ref<boolean>,
  emitFocusDone: (id: ID) => void,
  options: Options = {},
) {
  const block = options.block ?? "center";
  const behavior = options.behavior ?? "smooth";
  const doneDelayMs = options.doneDelayMs ?? 250;

  const itemRefs = new Map<ID, HTMLElement>();
  const lastFocusedId = ref<ID | null>(null);

  function setItemRef(id: ID, el: Element | ComponentPublicInstance | null) {
    const dom = el as unknown as HTMLElement | null;
    if (!dom) {
      itemRefs.delete(id);
      return;
    }
    itemRefs.set(id, dom);
  }

  watch(
    () => [focusIdRef.value, isLoadingRef.value] as const,
    async ([id, loading]) => {
      if (id == null) return;
      if (loading) return;

      if (lastFocusedId.value === id) return;
      lastFocusedId.value = id;

      await nextTick();

      const el = itemRefs.get(id);
      if (!el) {
        emitFocusDone(id);
        return;
      }

      el.scrollIntoView({ block, behavior });

      await new Promise((resolve) => window.setTimeout(resolve, doneDelayMs));

      emitFocusDone(id);
    },
    { immediate: true },
  );

  return {
    setItemRef,
  };
}
