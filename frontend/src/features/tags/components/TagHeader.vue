<template>
  <header
    class="sticky top-0 z-10 bg-background/90 backdrop-blur border-b border-border"
  >
    <div class="px-6 h-20 flex items-center gap-4">
      <!-- 왼쪽: back or icon -->
      <div class="shrink-0 flex items-center gap-3">
        <button
          v-if="showBack"
          type="button"
          class="inline-flex items-center gap-2 px-3 py-2 rounded-md transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800"
          @click="$emit('back')"
        >
          <span aria-hidden="true">←</span>
          <span class="text-sm font-medium">{{ backLabel }}</span>
        </button>

        <slot v-else name="left" />
      </div>

      <!-- 중앙: 제목 -->
      <div class="min-w-0 flex-1">
        <Transition name="taghead" mode="out-in">
          <div :key="variant" class="min-w-0">
            <h2 class="text-2xl font-bold leading-7 truncate">{{ title }}</h2>
            <p
              v-if="subtitle"
              class="mt-1 text-sm text-zinc-500 dark:text-zinc-400 truncate"
            >
              {{ subtitle }}
            </p>
          </div>
        </Transition>
      </div>

      <!-- 오른쪽 -->
      <div class="shrink-0 flex items-center gap-2"><slot name="right" /></div>
    </div>
  </header>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    variant: "dashboard" | "bookmarks";
    title: string;
    subtitle?: string;
    showBack?: boolean;
    backLabel?: string;
  }>(),
  { showBack: false, backLabel: "뒤로" },
);

defineEmits<{ (e: "back"): void }>();
</script>

<style scoped>
.taghead-enter-active,
.taghead-leave-active {
  transition:
    opacity 160ms ease,
    transform 160ms ease;
}
.taghead-enter-from,
.taghead-enter-to {
  opacity: 0;
  transform: translateY(4px);
}
</style>
