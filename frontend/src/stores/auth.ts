import { defineStore } from "pinia";
import http from "@/api/http";
import type { User } from "@/types/common";

type OAuthProvider = "google" | "kakao" | "naver";

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

    /** 소셜 OAuth 시작 */
    startOAuth(provider: OAuthProvider) {
      const apiBase = import.meta.env.VITE_APP_BASE_URL;

      const nonce = crypto.getRandomValues(new Uint32Array(2)).join("-");
      const redirect = sessionStorage.getItem("oauth:redirect") || "/workspace";
      sessionStorage.setItem("oauth:nonce", nonce);

      const state = base64url(JSON.stringify({ n: nonce, r: redirect }));

      window.location.href = `${apiBase}/oauth2/authorization/${provider}?state=${encodeURIComponent(
        state
      )}`;
    },

    /** AT/RT 재발급 */
    async refresh(): Promise<void> {
      if (this._refreshPromise) {
        return this._refreshPromise;
      }

      // 새 refresh 요청 시작
      this._refreshPromise = (async () => {
        const { data } = await http.post<{ accessToken: string }>(
          "/auth/refresh"
        );
        this.setAccessToken(data.accessToken);
      })().finally(() => {
        this._refreshPromise = null;
      });

      return this._refreshPromise;
    },

    /** 로그아웃 */
    async logout() {
      try {
        await http.post("/auth/logout");
      } catch (e) {
        console.warn("logout request failed:", e);
      } finally {
        this.setAccessToken(null);
        this.setUser(null);
      }
    },

    /** 앱 부팅/새로고침 시 세션 복원 */
    async restore() {
      try {
        await this.refresh();
        await this.fetchProfile();
      } catch {
        this.setAccessToken(null);
        this.setUser(null);
      } finally {
        this.restored = true;
      }
    },

    /** 사용자 프로필 조회 */
    async fetchProfile(force = false) {
      if (!force && this.user) return;
      const { data: me } = await http.get<User>("/users/me");
      this.setUser(me);
    },
  },
});

/** JSON 문자열을 URL-safe Base64로 변환(+ → -, / → _, = 제거) */
function base64url(s: string) {
  return btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
