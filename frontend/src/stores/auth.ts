import http, { unwrap } from "@/api/http";
import { defineStore } from "pinia";

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
    startOAuth(provider: OAuthProvider) {
      const apiBase = import.meta.env.VITE_API_BASE_URL;
      const redirectUri = `${window.location.origin}/redirect`;
      const url = `${apiBase}/oauth2/authorization/${provider}?redirect_uri=${encodeURIComponent(
        redirectUri
      )}`;
      window.location.href = url;
    },
    async completeOAuth() {
      const data = await unwrap<RefreshResponse>(
        http.post("/api/v1/auth/refresh")
      );
      this.setAccessToken(data.accessToken);
      this.setUser(data.user);
    },
    async logout(redirct = false) {
      try {
        await http.post("/api/v1/auth/logout");
      } catch (e) {
        console.warn("logout request failed:", e);
      }
      this.setAccessToken(null);
      this.setUser(null);
    },
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
    async fetchProfile() {
      const me = await unwrap<User>(http.get("/api/v1/users/me"));
      this.setUser(me);
    },
  },
});
