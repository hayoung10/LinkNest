import { ApiError } from "@/types/common";
import { AxiosError } from "axios";

export type HttpErrorType =
  | "timeout"
  | "canceled"
  | "network"
  | "server"
  | "unknown";

export class HttpError extends Error {
  type: HttpErrorType;
  status?: number;
  code?: string;
  path?: string;

  constructor(
    opts: {
      type: HttpErrorType;
      message: string;
      status?: number;
      code?: string;
      path?: string;
    },
    cause?: unknown,
  ) {
    super(opts.message);
    this.name = "HttpError";
    this.type = opts.type;
    this.status = opts.status;
    this.code = opts.code;
    this.path = opts.path;

    if (cause instanceof Error) {
      this.stack = cause.stack;
    }
  }
}

export function isHttpError(e: unknown): e is HttpError {
  return e instanceof HttpError;
}

export function toHttpError(e: unknown): HttpError {
  if (isHttpError(e)) return e;

  // AxiosError면 서버/네트워크 구분
  const ax = e as AxiosError<ApiError>;
  if (ax?.isAxiosError) {
    // 서버 응답이 있으면 server
    if (ax.response) {
      const data = ax.response.data;
      return new HttpError(
        {
          type: "server",
          message: data?.message ?? "서버 오류가 발생했습니다.",
          status: ax.response.status,
          code: data?.code,
          path: data?.path,
        },
        ax,
      );
    }

    // 응답이 없으면 network/unknown
    return new HttpError(
      {
        type: "network",
        message: "네트워크 연결을 확인해주세요.",
      },
      ax,
    );
  }

  if (e instanceof Error && e.message.trim()) {
    return new HttpError(
      {
        type: "unknown",
        message: e.message,
      },
      e,
    );
  }

  // 그 외
  return new HttpError(
    {
      type: "unknown",
      message: "알 수 없는 오류가 발생했습니다.",
    },
    e,
  );
}

export function getErrorMessage(
  e: unknown,
  fallback = "요청에 실패했습니다.",
): string {
  const he = toHttpError(e);
  return he.message || fallback;
}
