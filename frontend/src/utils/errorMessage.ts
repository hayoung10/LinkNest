import axios from "axios";

export function getErrorMessage(e: unknown, fallback: string) {
  if (axios.isAxiosError(e)) {
    const msg = (e.response?.data as any)?.message;
    if (typeof msg === "string" && msg.trim()) return msg;
  }

  if (e instanceof Error && e.message.trim()) return e.message;
  return fallback;
}
