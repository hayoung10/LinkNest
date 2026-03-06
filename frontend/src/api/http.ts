import axios, {
  AxiosError,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
  type AxiosRequestConfig,
  AxiosResponse,
} from "axios";
import type { ApiSuccess, ApiError } from "@/types/common";
import { useAuthStore } from "@/stores/auth";
import { HttpError, toHttpError } from "./errors";

const BASE_URL = import.meta.env.VITE_APP_BASE_URL as string;
const API_PREFIX = (import.meta.env.VITE_API_PREFIX as string) ?? "/api/v1";

function joinURL(base: string, path: string) {
  const b = base.replace(/\/+$/, "");
  const p = path.startsWith("/") ? path : `/${path}`;
  return `${b}${p}`;
}

const BASE_API = joinURL(BASE_URL, API_PREFIX);

// Axios instance (공통 설정)
const http: AxiosInstance = axios.create({
  baseURL: BASE_API,
  withCredentials: true,
  timeout: 10000,
});

// 요청 인터셉터: Access Token 자동 부착
http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const auth = useAuthStore();
  if (auth.accessToken) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${auth.accessToken}`;
  }
  return config;
});

// 응답 인터셉터
http.interceptors.response.use(
  (res) => res,
  async (error: AxiosError<ApiError>) => {
    const auth = useAuthStore();
    const original = error.config as AxiosRequestConfig & { _retry?: boolean };

    // /auth/refresh 요청이 401로 실패할 때, 무한 재시도 방지
    const isRefreshCall =
      typeof original?.url === "string" &&
      original.url.includes("/auth/refresh");

    // 1. Timeout 처리
    if (error.code === "ECONNABORTED") {
      return Promise.reject(
        new HttpError({
          type: "timeout",
          message: "요청이 지연되고 있습니다. 잠시 후 다시 시도해주세요.",
        }),
      );
    }

    // 2. Cancel 처리
    if (error.code === "ERR_CANCELED") {
      return Promise.reject(
        new HttpError({
          type: "canceled",
          message: "요청이 취소되었습니다.",
        }),
      );
    }

    // 3. 네트워크 에러 (서버 응답 없음)
    if (!error.response) {
      return Promise.reject(
        new HttpError({
          type: "network",
          message: "네트워크 연결을 확인해주세요.",
        }),
      );
    }

    // 4. 401 처리 (RT 재발급)
    if (error.response?.status === 401 && !original._retry && !isRefreshCall) {
      original._retry = true;
      try {
        await auth.refresh();
        return http(original); // 원래 요청 재시도
      } catch (e) {
        await auth.logout();
        return Promise.reject(e);
      }
    }

    // 5. 그 외 서버 에러
    return Promise.reject(error);
  },
);

export default http;

export async function unwrap<T>(
  p: Promise<AxiosResponse<ApiSuccess<T>>>,
): Promise<T> {
  try {
    const res = await p;
    return res.data.data;
  } catch (e) {
    throw toHttpError(e);
  }
}
