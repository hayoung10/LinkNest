import type { Bookmark, ID } from "@/types/common";

type FetchBookmark = (id: ID) => Promise<Bookmark>;

export function refreshBookmarkAutoImage(
  id: ID,
  fetchBookmark: FetchBookmark,
  onReady: (latest: Bookmark) => void,
  options?: {
    delays?: number[];
  },
) {
  const delays = options?.delays ?? [400, 1200, 2500];
  let done = false;

  for (const delay of delays) {
    setTimeout(async () => {
      if (done) return;

      try {
        const latest = await fetchBookmark(id);

        const ready =
          latest.imageMode === "AUTO" && !!latest.autoImageUrl?.trim();

        if (!ready) return;

        done = true;
        onReady(latest);
      } catch {
        // 조용히 무시
      }
    }, delay);
  }
}
