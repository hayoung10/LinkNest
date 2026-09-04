<template>
  <main class="min-h-screen flex items-center justify-center bg-indigo-50">
    <section class="w-full px-4">
      <LoginCard title="로그인" description="소셜 계정으로 로그인하세요.">
        <LoginSocialButtons @google="login('google')" @kakao="login('kakao')" />

        <div class="my-5 flex items-center gap-3">
          <div class="h-px flex-1 bg-gray-200"></div>
          <span class="text-xs text-gray-400">또는</span>
          <div class="h-px flex-1 bg-gray-200"></div>
        </div>

        <button
          type="button"
          class="w-full h-14 rounded-xl px-6 flex items-center justify-center gap-3 border border-indigo-200 bg-indigo-50 text-indigo-700 hover:bg-indigo-100 active-translate-y-[0.5px] focus-visible:ring-4 focus-visible:ring-indigo-400/25 transition"
          @click="testLogin"
        >
          <span class="font-medium text-[15px]">{{
            isTestLoginPending ? "로그인 중…" : "테스트 계정으로 로그인"
          }}</span>
        </button>
        <p class="mt-5 text-center text-sm text-gray-500">
          로그인하면 이용약관 및 개인정보처리방침에 동의하게 됩니다
        </p>
      </LoginCard>
    </section>
  </main>
</template>

<script setup lang="ts">
import LoginCard from "@/components/auth/LoginCard.vue";
import LoginSocialButtons from "@/components/auth/LoginSocialButtons.vue";
import { useAuthStore } from "@/stores/auth";
import { useToastStore } from "@/stores/toast";
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { OAuthProvider } from "@/types/common";

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const toast = useToastStore();

const isTestLoginPending = ref(false);

// 새로고침 시 세션 복원 -> 이미 로그인 상태면 대시보드로 우회
onMounted(async () => {
  if (!auth.restored) {
    try {
      await auth.restore();
    } catch {
      /* noop: restore 실패 시 무시 (로그인 안 된 상태) */
    }
  }
  if (auth.isLoggedIn) {
    await router.replace("/");
    return;
  }
});

function login(provider: OAuthProvider) {
  auth.startOAuth(provider);
}

async function testLogin() {
  if (isTestLoginPending.value) return;

  isTestLoginPending.value = true;

  try {
    await auth.testLogin();

    const target = resolveRedirectTarget();
    await router.replace(target);
  } catch {
    toast.error("테스트 계정으로 로그인할 수 없습니다.");
  } finally {
    isTestLoginPending.value = false;
  }
}

function resolveRedirectTarget(): string {
  const redirect =
    typeof route.query.redirect === "string" ? route.query.redirect : null;

  if (redirect && isSafeInternalPath(redirect)) {
    return redirect;
  }

  return "/workspace";
}

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
</script>
