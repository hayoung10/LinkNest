import { defineStore } from "pinia";
import http, { unwrap } from "@/api/http";
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
    isLoggedIn: (s) => !!s.accessToken && !!s.user,
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
      const redirect =
        location.pathname === "/login"
          ? "/" // TODO: 다른 보호 라우트 생기면 이전 경로로 복귀하도록 수정
          : location.pathname + location.search;
      sessionStorage.setItem("oauth:nonce", nonce);
      sessionStorage.setItem("oauth:redirect", redirect);

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
        const { accessToken } = await unwrap<{ accessToken: string }>(
          http.post("/api/v1/auth/refresh")
        );
        this.setAccessToken(accessToken);
      })().finally(() => {
        this._refreshPromise = null;
      });

      return this._refreshPromise;
    },

    /** 로그아웃 */
    async logout() {
      try {
        await http.post("/api/v1/auth/logout");
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
      const me = await unwrap<User>(http.get("/api/v1/users/me"));
      this.setUser(me);
    },
  },
});

/** JSON 문자열을 URL-safe Base64로 변환(+ → -, / → _, = 제거) */
function base64url(s: string) {
  return btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
