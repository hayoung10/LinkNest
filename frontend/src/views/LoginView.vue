<template>
  <main class="min-h-screen flex items-center justify-center bg-indigo-50">
    <section class="w-full px-4">
      <LoginCard title="로그인" description="소셜 계정으로 로그인하세요.">
        <LoginSocialButtons
          @google="login('google')"
          @kakao="login('kakao')"
          @naver="login('naver')"
        />
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
import { onMounted } from "vue";
import { useRouter } from "vue-router";

const auth = useAuthStore();
const router = useRouter();

type Provider = "google" | "kakao" | "naver";

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
    router.replace("/");
  }
});

function login(provider: Provider) {
  auth.startOAuth(provider);
}
</script>
