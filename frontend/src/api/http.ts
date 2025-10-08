import axios, {
  AxiosError,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
  type AxiosRequestConfig,
} from "axios";
import type { ApiSuccess, ApiError } from "@/types/common";
import { useAuthStore } from "@/stores/auth";

// Axios instance (공통 설정)
const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_URL,
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
let isRefreshing = false; // 동시 중복 refresh 요청 방지 플래그

http.interceptors.response.use(
  (res) => res,
  async (error: AxiosError<ApiError>) => {
    const auth = useAuthStore();
    const original = error.config as AxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !original._retry) {
      if (isRefreshing) throw error;
      original._retry = true;
      isRefreshing = true;
      try {
        await auth.refresh();
        isRefreshing = false;
        return http(original); // 원래 요청 재시도
      } catch (e) {
        isRefreshing = false;
        await auth.logout(true);
      }
    }
    throw error;
  }
);

export default http;

// 성공 응답에서 data만 추출
export async function unwrap<T>(
  p: Promise<{ data: ApiSuccess<T> }>
): Promise<T> {
  const { data } = await p;
  return data.data;
}
