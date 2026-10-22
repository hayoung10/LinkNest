<template>
  <header
    class="flex min-h-0 shrink-0 flex-col gap-1 border-b border-border px-3 py-1.5 md:h-16 md:flex-row md:items-center md:gap-4 md:px-6 md:py-0"
  >
    <!-- 모바일 첫 번째 줄: 뒤로 가기 + 유저 메뉴 -->
    <div
      class="order-1 flex w-full items-center justify-between gap-2 md:contents"
    >
      <!-- 뒤로 가기 -->
      <button
        type="button"
        class="inline-flex shrink-0 items-center gap-2 rounded-md px-2 py-1 transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800 md:px-3"
        @click="$emit('back')"
      >
        <span aria-hidden="true">←</span>
        <span class="text-sm font-medium">{{ backLabel }}</span>
      </button>

      <!-- 우측: 유저 메뉴 -->
      <div class="shrink-0 md:order-3">
        <UserMenu
          :show-tag-management="showTagManagement"
          :show-trash="showTrash"
          @open-tag-management="$emit('open-tag-management')"
          @open-trash="$emit('open-trash')"
          @open-settings="$emit('open-settings')"
          @logout="$emit('logout')"
        />
      </div>
    </div>

    <!-- 모바일 두번째 줄: 타이틀 -->
    <div class="order-2 flex min-w-0 items-end gap-2 md:ml-32 md:flex-1">
      <slot name="title-leading" />

      <div class="flex min-w-0 flex-1 flex-col justify-end">
        <h1 class="truncate text-base font-semibold leading-5 sm:text-lg">
          {{ title }}
        </h1>
        <p
          v-if="subtitle"
          class="truncate text-xs leading-4 text-muted-foreground"
        >
          {{ subtitle }}
        </p>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import UserMenu from "@/features/workspace/menus/UserMenu.vue";

withDefaults(
  defineProps<{
    title: string;
    subtitle?: string;

    showBack?: boolean;
    backLabel?: string;

    showTagManagement?: boolean;
    showTrash?: boolean;
  }>(),
  {
    showBack: false,
    backLabel: "뒤로",
    showTagManagement: true,
    showTrash: true,
  },
);

defineEmits<{
  (e: "back"): void;
  (e: "open-settings"): void;
  (e: "open-trash"): void;
  (e: "open-tag-management"): void;
  (e: "logout"): void;
}>();
</script>

<style scoped>
.pagehead-enter-active,
.pagehead-leave-active {
  transition:
    opacity 160ms ease,
    transform 160ms ease;
}
.pagehead-enter-from,
.pagehead-enter-to {
  opacity: 0;
  transform: translateY(4px);
}
.pagehead-enter-to,
.pagehead-enter-from {
  opacity: 1;
  transform: translateY(0);
}
</style>
