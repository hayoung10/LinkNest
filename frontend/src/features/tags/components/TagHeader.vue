<template>
  <header
    class="sticky top-0 z-10 bg-background/90 backdrop-blur border-b border-border"
  >
    <div class="flex h-16 items-center gap-2 px-4 sm:h-20 sm:gap-4 sm:px-6">
      <!-- 왼쪽: back or icon -->
      <div class="flex shrink-0 items-center gap-2 sm:gap-3">
        <button
          v-if="showBack"
          type="button"
          class="inline-flex items-center gap-1 px-2 py-1.5 rounded-md transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800 sm:gap-2 sm:px-3 sm:py-2"
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
            <h2
              class="truncate text-xl font-bold leading-6 s:text-2xl sm:leading-7"
            >
              {{ title }}
            </h2>
            <p
              v-if="subtitle"
              class="mt-0.5 truncate text-xs text-zinc-500 dark:text-zinc-400 sm:mt-1 sm:text-sm"
            >
              {{ subtitle }}
            </p>
          </div>
        </Transition>
      </div>

      <!-- 오른쪽 -->
      <div class="flex shrink-0 items-center gap-1 sm:gap-2">
        <slot name="right" />
      </div>
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
