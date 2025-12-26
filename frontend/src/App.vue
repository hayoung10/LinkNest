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

    <!-- 전역 Toast -->
    <Toast />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useAuthStore } from "./stores/auth";
import { RouterView, useRoute } from "vue-router";
import Toast from "@/components/ui/Toast.vue";
import { ToastType, useToastStore } from "./stores/toast";

const route = useRoute();
const toast = useToastStore();
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

type ToastState = { toast?: { type: ToastType; message: string } };

watch(
  () => route.fullPath,
  () => {
    const st = history.state as ToastState | null;
    const t = st?.toast;
    if (!t?.message) return;

    toast.show(t.message, t.type);

    const next = { ...(history.state ?? {}) };
    delete (next as any).toast;
    history.replaceState(next, "");
  },
  { immediate: true }
);
</script>

<style scoped>
/* 최소 스타일 */
</style>
