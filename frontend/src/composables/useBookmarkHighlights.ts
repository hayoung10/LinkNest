import { Ref } from "vue";
import { splitHighlight } from "@/lib/highlight";
import type { Bookmark } from "@/types/common";

type Chunk = { text: string; isHit: boolean };

type Options = {
  searchQ: Ref<string>;
  getTitle?: (b: Bookmark) => string;
  getDomain?: (b: Bookmark) => string;
  highlightClass?: string;
};

export function useBookmarkHighlights(options: Options) {
  const highlightClass =
    options.highlightClass ??
    "font-extrabold bg-yellow-200/40 dark:bg-yellow-400/20 rounded";

  function titleChunks(b: Bookmark): Chunk[] {
    const text = options.getTitle ? options.getTitle(b) : (b.title ?? "");
    return splitHighlight(text, options.searchQ.value);
  }

  function domainChunks(b: Bookmark): Chunk[] {
    const text = options.getDomain ? options.getDomain(b) : b.url;
    return splitHighlight(text, options.searchQ.value);
  }

  function tagChunks(tag: string): Chunk[] {
    return splitHighlight(tag, options.searchQ.value);
  }

  return {
    highlightClass,
    titleChunks,
    domainChunks,
    tagChunks,
  };
}
