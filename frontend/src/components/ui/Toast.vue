<template>
  <div
    v-if="toasts.length"
    class="fixed left-1/2 -translate-x-1/2 top-4 z-[9999] flex w-[360px] max-w-[calc(100vw-2rem)] flex-col gap-2"
    role="status"
    aria-live="polite"
    aria-relevant="additions"
  >
    <div
      v-for="t in toasts"
      :key="t.id"
      class="rounded-xl bg-white dark:bg-zinc-900 text-card-foreground shadow-lg border border-border px-4 py-3 flex items-center gap-3"
      :class="typeStyle(t.type)"
    >
      <!-- 아이콘-->
      <div class="shrink-0 leading-none text-base">
        <span v-if="t.type === 'error'">⛔</span>
        <span v-else-if="t.type === 'success'">✅</span>
        <span v-else>ℹ️</span>
      </div>

      <div class="min-w-0 flex-1 text-sm font-medium leading-5">
        {{ t.message }}
      </div>

      <button
        type="button"
        class="shrink-0 self-center rounded-md p-1 hover:bg-accent"
        aria-label="닫기"
        @click="dismiss(t.id)"
      >
        <CloseIcon class="size-4" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ToastType, useToastStore } from "@/stores/toast";
import { storeToRefs } from "pinia";
import CloseIcon from "@/components/icons/CloseIcon.vue";

const toast = useToastStore();
const { toasts } = storeToRefs(toast);

function typeStyle(t: ToastType) {
  if (t === "error")
    return "border-red-200/70 dark:border-red-900/40 bg-red-50/70 dark:bg-red-950/20 border-l-4 border-l-red-500";
  if (t === "success")
    return "border-emerald-200/70 dark:border-emerald-900/40 bg-emerald-50/70 dark:bg-emerald-950/20 border-l-4 border-l-emerald-500";
  return "border-border bg-card border-l-4 border-l-blue-500/60";
}

function dismiss(id: string) {
  toast.dismiss(id);
}
</script>
