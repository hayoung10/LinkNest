<template>
  <div class="min-h-screen">
    <!-- 복원 대기 화면 -->
    <div
      v-if="!ready"
      class="flex items-center justify-center h-screen text-muted-foreground"
    >
      <span class="animate-pulse">세션 복원 중...</span>
    </div>

    <!-- 복원 완료 후 라우터 렌더-->
    <RouterView v-else />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useAuthStore } from "./stores/auth";
import { RouterView } from "vue-router";

const auth = useAuthStore();
const ready = ref(false);

onMounted(async () => {
  try {
    if (!auth.restored) {
      await auth.restore();
    }
  } finally {
    ready.value = true; // 복원 성공/실패와 무관하게 화면은 진행
  }
});
</script>

<style scoped>
/* 최소 스타일 */
</style>
