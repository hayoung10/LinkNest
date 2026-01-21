import { defineStore } from "pinia";
import http, { unwrap } from "@/api/http";
import type { User } from "@/types/common";
import * as UserApi from "@/api/users";

type OAuthProvider = "google" | "kakao";

type TokenRefreshRes = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
};

export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: null as string | null,
    user: null as User | null,
    restored: false, // 앱 부팅/새로고침 시 복원 여부
    _refreshPromise: null as Promise<void> | null,
  }),
  getters: {
    isLoggedIn: (s) => !!s.accessToken,
    hasProfile: (s) => !!s.user,
  },
  actions: {
    setAccessToken(token: string | null) {
      this.accessToken = token;
    },
    setUser(user: User | null) {
      this.user = user;
    },
    clearSession() {
      this.accessToken = null;
      this.user = null;
    },

    /** 소셜 OAuth 시작 */
    startOAuth(provider: OAuthProvider) {
      const backendBaseUrl = import.meta.env.VITE_APP_BASE_URL as string;

      const nonce = crypto.getRandomValues(new Uint32Array(2)).join("-");
      const redirect = sessionStorage.getItem("oauth:redirect") || "/workspace";
      sessionStorage.setItem("oauth:nonce", nonce);

      const state = base64url(JSON.stringify({ n: nonce, r: redirect }));

      window.location.href = `${backendBaseUrl}/oauth2/authorization/${provider}?state=${encodeURIComponent(
        state,
      )}`;
    },

    /** AT/RT 재발급 */
    async refresh(): Promise<void> {
      if (this._refreshPromise) {
        return this._refreshPromise;
      }

      // 새 refresh 요청 시작
      this._refreshPromise = (async () => {
        const data = await unwrap<TokenRefreshRes>(http.post("/auth/refresh"));
        this.setAccessToken(data.accessToken);
      })().finally(() => {
        this._refreshPromise = null;
      });

      return this._refreshPromise;
    },

    /** 로그아웃 */
    async logout() {
      try {
        await unwrap<void>(http.post("/auth/logout"));
      } catch (e) {
        console.warn("logout request failed:", e);
      } finally {
        this.clearSession();
      }
    },

    /** 모든 기기에서 로그아웃 */
    async logoutAllSessions() {
      try {
        await unwrap<void>(http.delete("/auth/sessions"));
      } catch (e) {
        console.warn("logout all sessions request failed:", e);
      } finally {
        this.clearSession();
      }
    },

    /** 앱 부팅/새로고침 시 세션 복원 */
    async restore() {
      try {
        await this.refresh();
        await this.fetchProfile();
      } catch {
        this.clearSession();
      } finally {
        this.restored = true;
      }
    },

    /** 사용자 프로필 조회 */
    async fetchProfile(force = false) {
      if (!force && this.user) return;

      const me = await UserApi.getMe();
      this.setUser(me);
    },
  },
});

/** JSON 문자열을 URL-safe Base64로 변환(+ → -, / → _, = 제거) */
function base64url(s: string) {
  return btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
