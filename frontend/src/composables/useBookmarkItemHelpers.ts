import { computed } from "vue";
import { useWorkspaceStore } from "@/stores/workspace";
import type { Bookmark } from "@/types/common";

type Options = {
  locale?: string;
};

export function useBookmarkItemHelpers(options: Options = {}) {
  const workspace = useWorkspaceStore();

  const locale = options.locale ?? "ko-KR";

  function displayTitle(b: Bookmark): string {
    const t = (b.title ?? "").trim();
    return t || "(제목 없음)";
  }

  function hasTitle(b: Bookmark): boolean {
    return !!b.title?.trim();
  }

  function domain(url: string) {
    try {
      return new URL(url).host.replace(/^www\./, "");
    } catch {
      return url;
    }
  }

  function formatDate(iso?: string): string {
    if (!iso) return "-";
    try {
      return new Intl.DateTimeFormat(locale, {
        year: "numeric",
        month: "numeric",
        day: "numeric",
      }).format(new Date(iso));
    } catch {
      return "-";
    }
  }

  function coverUrl(b: Bookmark): string | null {
    if (b.imageMode === "CUSTOM") return b.customImageUrl ?? null;
    if (b.imageMode === "AUTO") return b.autoImageUrl ?? null;
    return null;
  }

  function isAutoPending(b: Bookmark): boolean {
    return b.imageMode === "AUTO" && !b.autoImageUrl;
  }

  function tagCount(b: Bookmark) {
    return b.tags?.length ?? 0;
  }

  function visibleTags(b: Bookmark, visible = 3) {
    return b.tags?.slice(0, visible) ?? [];
  }

  function extraTagCount(b: Bookmark, visible = 3) {
    return Math.max(tagCount(b) - visible, 0);
  }

  // ------------------------
  // Favorites
  // ------------------------
  function collectionEmoji(b: Bookmark): string {
    return workspace.collectionInfoById[b.collectionId]?.emoji ?? "📁";
  }

  function collectionName(b: Bookmark): string {
    return (
      workspace.collectionInfoById[b.collectionId]?.name ?? "로딩…"
    ).trim();
  }

  function collectionLabel(b: Bookmark): string {
    return `${collectionEmoji(b)} ${collectionName(b)}`;
  }

  return {
    displayTitle,
    hasTitle,
    domain,
    formatDate,
    coverUrl,
    isAutoPending,
    tagCount,
    visibleTags,
    extraTagCount,

    collectionEmoji,
    collectionName,
    collectionLabel,
  };
}
