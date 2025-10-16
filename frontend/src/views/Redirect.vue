<template>
  <div class="hidden">Redirecting...</div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

onMounted(async () => {
  if (typeof route.query.error === "string" && route.query.error) {
    router.replace({ name: "login", query: { error: route.query.error } });
    return;
  }

  try {
    await auth.restore();
    const target = resolveRedirectTarget();
    clearOAuthSession();
    router.replace(target);
  } catch {
    await auth.logout();
    router.replace({ name: "login", query: { error: "oauth_failed" } });
  }
});

/** 최종 이동 경로 결정 */
function resolveRedirectTarget(): string {
  // 1) nonce 검증
  const rFromState = getRFromStateIfValid();
  if (rFromState) return rFromState;

  // 2) query.redirect
  const q =
    typeof route.query.redirect === "string" ? route.query.redirect : null;
  if (q && isSafeInternalPath(q)) return q;

  // 3) sessionStorage
  const redirectStored = sessionStorage.getItem("oauth:redirect");
  if (redirectStored && isSafeInternalPath(redirectStored))
    return redirectStored;

  return "/workspace";
}

/** redirect 경로 복원 */
function getRFromStateIfValid(): string | null {
  const raw = typeof route.query.state === "string" ? route.query.state : null;
  if (!raw) return null;

  try {
    const json = base64urlDecode(raw);
    const { n, r } = JSON.parse(json) as { n?: string; r?: string };
    const nonceStored = sessionStorage.getItem("oauth:nonce");
    if (!n || !nonceStored || n !== nonceStored) return null;
    if (r && isSafeInternalPath(r)) return r;
  } catch {}
  return null;
}

/** base64url → utf-8 문자열 */
function base64urlDecode(input: string): string {
  const base64 = input.replace(/-/g, "+").replace(/_/g, "/");
  const padding =
    base64.length % 4 === 2 ? "==" : base64.length % 4 === 3 ? "=" : "";
  return atob(base64 + padding);
}

/** 현재 오리지 내부 경로만 허용 */
function isSafeInternalPath(path: string): boolean {
  try {
    const url = new URL(path, window.location.origin);
    return (
      url.origin === window.location.origin && url.pathname.startsWith("/")
    );
  } catch {
    return false;
  }
}

function clearOAuthSession() {
  sessionStorage.removeItem("oauth:nonce");
  sessionStorage.removeItem("oauth:redirect");
}
</script>
