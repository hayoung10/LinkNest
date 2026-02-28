import { isHttpError } from "@/api/errors";

export function getErrorMessage(e: unknown, fallback: string) {
  if (isHttpError(e)) {
    return e.message;
  }

  if (e instanceof Error && e.message.trim()) {
    return e.message;
  }

  return fallback;
}
