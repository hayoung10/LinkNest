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

export interface User {
  id: number;
  name: string;
  // 백엔드 User 엔티티와 맞춰서 필드 확장 예정
}
