import { useToastStore } from "@/stores/toast";
import { useWorkspaceStore } from "@/stores/workspace";
import type { Bookmark, ID } from "@/types/common";
import { computed, ComputedRef } from "vue";

type Options = {
  disabled?: ComputedRef<boolean> | boolean;
  onToggled?: (payload: { id: ID; isFavorite: boolean }) => void; // 토글 성공 후 후처리
};

export function useBookmarkFavorite(options: Options = {}) {
  const workspace = useWorkspaceStore();
  const toast = useToastStore();

  const disabled = computed(() =>
    typeof options.disabled === "boolean"
      ? options.disabled
      : (options.disabled?.value ?? false),
  );

  const isFavoriteMutating = computed(
    () => disabled.value || workspace.isMutating.toggleBookmarkFavorite,
  );

  function isMutatingFor(_id: ID) {
    return isFavoriteMutating.value;
  }

  async function toggleFavorite(b: Bookmark) {
    if (isMutatingFor(b.id)) return;

    try {
      const updated = await workspace.toggleBookmarkFavorite(b);
      options.onToggled?.({ id: b.id, isFavorite: updated.isFavorite });
    } catch (e) {
      toast.error("즐겨찾기 변경에 실패했습니다.");
    }
  }

  return {
    isMutatingFor,
    toggleFavorite,
  };
}
