import { useToastStore } from "@/stores/toast";
import { useWorkspaceStore } from "@/stores/workspace";
import type { Bookmark, ID } from "@/types/common";
import { computed } from "vue";

export function useBookmarkFavorite() {
  const workspace = useWorkspaceStore();
  const toast = useToastStore();

  const isFavoriteMutating = computed(
    () => workspace.isMutating.toggleBookmarkFavorite,
  );

  function isMutatingFor(_id: ID) {
    return isFavoriteMutating.value;
  }

  async function toggleFavorite(b: Bookmark) {
    if (isMutatingFor(b.id)) return;

    try {
      await workspace.toggleBookmarkFavorite(b);
    } catch (e) {
      toast.error("즐겨찾기 변경에 실패했습니다.");
    }
  }

  return {
    isMutatingFor,
    toggleFavorite,
  };
}
