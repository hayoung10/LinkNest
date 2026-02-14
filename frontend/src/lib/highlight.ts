export type HighlightChunk = { text: string; isHit: boolean };

export function escapeRegExp(s: string) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

export function splitHighlight(text: string, q: string): HighlightChunk[] {
  const raw = (text ?? "").toString().normalize("NFC");
  const keyword = (q ?? "").toString().trim().normalize("NFC");

  if (!keyword) return [{ text: raw, isHit: false }];

  const regex = new RegExp(`(${escapeRegExp(keyword)})`, "gi");
  const parts = raw.split(regex);

  const lower = keyword.toLowerCase();

  return parts
    .filter((p) => p.length > 0)
    .map((part) => ({
      text: part,
      isHit: part.toLowerCase() === lower,
    }));
}
