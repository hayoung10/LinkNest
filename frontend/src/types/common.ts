// API 성공/실패 응답 규약
export type ApiSuccess<T> = {
  status: number;
  code: "OK";
  message: string;
  data: T;
};

export type ApiError = {
  status: number;
  code: string;
  message: string;
  path?: string;
  timestamp?: string;
};
