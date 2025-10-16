import axios, {
  AxiosError,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
  type AxiosRequestConfig,
} from "axios";
import type { ApiSuccess, ApiError } from "@/types/common";
import { useAuthStore } from "@/stores/auth";

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

// 응답 인터셉터: 401 -> Refresh Token 재발급
http.interceptors.response.use(
  (res) => res,
  async (error: AxiosError<ApiError>) => {
    const auth = useAuthStore();
    const original = error.config as AxiosRequestConfig & { _retry?: boolean };

    // /auth/refresh 요청이 401로 실패할 때, 무한 재시도 방지
    const isRefreshCall =
      typeof original?.url === "string" &&
      original.url.includes("/auth/refresh");

    if (error.response?.status === 401 && !original._retry && !isRefreshCall) {
      original._retry = true;
      try {
        await auth.refresh();
        return http(original); // 원래 요청 재시도
      } catch (e) {
        await auth.logout();
        throw e;
      }
    }
    throw error;
  }
);

export default http;

// // 성공 응답에서 data만 추출
// export async function unwrap<T>(
//   p: Promise<{ data: ApiSuccess<T> }>
// ): Promise<T> {
//   const { data } = await p;
//   return data.data;
// }
