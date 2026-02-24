<template>
  <header class="h-16 border-b border-border px-6 flex items-center gap-4">
    <!-- 뒤로 -->
    <button
      type="button"
      class="inline-flex items-center gap-2 px-3 py-2 rounded-md transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800"
      @click="$emit('back')"
    >
      <span aria-hidden="true">←</span>
      <span class="text-sm font-medium">{{ backLabel }}</span>
    </button>

    <!-- 타이틀 -->
    <div class="min-w-0">
      <h1 class="text-lg font-semibold leading-6 truncate">{{ title }}</h1>
      <p
        v-if="subtitle"
        class="text-xs text-muted-foreground leading-4 truncate"
      >
        {{ subtitle }}
      </p>
    </div>

    <div class="flex-1" />

    <!-- 우측: 유저 메뉴 -->
    <UserMenu
      :show-tag-management="showTagManagement"
      :show-trash="showTrash"
      @open-tag-management="$emit('open-tag-management')"
      @open-trash="$emit('open-trash')"
      @open-settings="$emit('open-settings')"
      @logout="$emit('logout')"
    />
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
    showTagmanagement: true,
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
