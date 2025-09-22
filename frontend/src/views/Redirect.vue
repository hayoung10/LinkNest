<template>
  <div style="padding: 16px">
    <p>로그인 처리 중입니다...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

onMounted(async () => {
  try {
    // 서버가 RT 쿠키를 심어줬다는 전제 하에 /auth/refresh로 AT + user 확정
    await auth.completeOAuth();

    // 원래 가려던 곳으로 보내주기 (login에서 ?redirect= 저장한 경우 대비)
    const to = (route.query.redirect as string) || "/";
    await router.replace(to);
  } catch {
    await router.replace({ name: "login" });
  }
});
</script>
