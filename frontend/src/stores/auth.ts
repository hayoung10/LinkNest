import { defineStore } from "pinia";
import http, { unwrap } from "@/api/http";
import type { User } from "@/types/common";

type OAuthProvider = "google" | "kakao" | "naver";

type RefreshResponse = {
  accessToken: string;
  user: User;
};

export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: null as string | null,
    user: null as User | null,
    restored: false, // 앱 시작/새로고침 시 복원 여부
    _refreshPromise: null as Promise<string> | null,
  }),
  getters: {
    isLoggedIn: (s) => !!s.accessToken && !!s.user,
  },
  actions: {
    setAccessToken(token: string | null) {
      this.accessToken = token;
    },
    setUser(user: User | null) {
      this.user = user;
    },

    // 소셜 OAuth 시작
    startOAuth(provider: OAuthProvider) {
      const apiBase = import.meta.env.VITE_APP_BASE_URL;
      const redirectUri = `${window.location.origin}/redirect`;
      const url = `${apiBase}/oauth2/authorization/${provider}?redirect_uri=${encodeURIComponent(
        redirectUri
      )}`;
      window.location.href = url;
    },

    // 소셜 OAuth 완료 후 AT/유저 최신화
    async completeOAuth() {
      const data = await unwrap<RefreshResponse>(
        http.post("/api/v1/auth/refresh")
      );
      this.setAccessToken(data.accessToken);
      this.setUser(data.user);
    },

    // 인터셉터에서 호출되는 토큰 재발급
    async refresh(): Promise<string> {
      if (this._refreshPromise) {
        return this._refreshPromise;
      }

      // 새 refresh 요청 시작
      this._refreshPromise = (async () => {
        const data = await unwrap<RefreshResponse>(
          http.post("/api/v1/auth/refresh")
        );
        this.setAccessToken(data.accessToken);
        this.setUser(data.user);
        return data.accessToken;
      })();

      // _refreshPromise 초기화
      try {
        return await this._refreshPromise;
      } finally {
        this._refreshPromise = null;
      }
    },

    // 로그아웃
    async logout(redirct = false) {
      try {
        await http.post("/api/v1/auth/logout");
      } catch (e) {
        console.warn("logout request failed:", e);
      }
      this.setAccessToken(null);
      this.setUser(null);
    },

    // 앱 부팅/새로고침 시 세션 복원
    async restore() {
      try {
        const data = await unwrap<RefreshResponse>(
          http.post("/api/v1/auth/refresh")
        );
        this.setAccessToken(data.accessToken);
        this.setUser(data.user);
      } catch {
        this.setAccessToken(null);
        this.setUser(null);
      } finally {
        this.restored = true;
      }
    },

    // 프로필 조회
    async fetchProfile() {
      const me = await unwrap<User>(http.get("/api/v1/users/me"));
      this.setUser(me);
    },
  },
});
