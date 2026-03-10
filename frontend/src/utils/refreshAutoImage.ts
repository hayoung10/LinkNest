import type { Bookmark, ID } from "@/types/common";

type FetchBookmark = (id: ID) => Promise<Bookmark>;

export function refreshBookmarkAutoImage(
  id: ID,
  fetchBookmark: FetchBookmark,
  onResolved: (latest: Bookmark) => void,
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
        if (done) return;

        if (latest.imageMode !== "AUTO") {
          done = true;
          onResolved(latest);
          return;
        }

        // 생성 성공
        if (latest.autoImageStatus === "SUCCESS") {
          done = true;
          onResolved(latest);
          return;
        }

        // 생성 실패
        if (latest.autoImageStatus === "FAILED") {
          done = true;
          onResolved(latest);
          return;
        }

        // PENDING이면 다음 타이머까지 대기
      } catch {
        // 조용히 무시
      }
    }, delay);
  }
}
