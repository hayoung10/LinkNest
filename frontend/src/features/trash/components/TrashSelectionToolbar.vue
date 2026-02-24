<template>
  <div
    v-if="selectedCount > 0"
    class="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-border bg-background/95 backdrop-blur px-3 py-2"
  >
    <!-- 왼쪽: 전체 선택 -->
    <button
      type="button"
      class="text-sm px-3 py-2 rounded-md border border-zinc-300 text-foreground hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
      @click="$emit('toggle-all')"
    >
      {{ allChecked ? "전체 해제" : "전체 선택" }}
    </button>

    <!-- 오른쪽: 선택 정보 + 액션 -->
    <div class="flex flex-wrap items-center gap-2">
      <div class="text-sm text-neutral-500 dark:text-neutral-400">
        {{ selectedCount }}개 선택됨
      </div>

      <button
        type="button"
        class="text-sm px-3 py-2 rounded-md border transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        :class="
          selectedCount > 0 && !isMutating
            ? 'border-blue-300 bg-blue-50 text-blue-600 hover:bg-blue-100'
            : 'border-border'
        "
        :disabled="selectedCount === 0 || isMutating"
        @click="$emit('restore')"
      >
        복구
      </button>

      <button
        type="button"
        class="text-sm px-3 py-2 rounded-md border transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        :class="
          selectedCount > 0 && !isMutating
            ? 'border-red-400 bg-red-50 text-red-700 hover:bg-red-100'
            : 'border-border'
        "
        :disabled="selectedCount === 0 || isMutating"
        @click="$emit('delete')"
      >
        영구 삭제
      </button>

      <button
        type="button"
        class="text-sm px-3 py-2 rounded-md border border-zinc-200 text-neutral-500 hover:bg-zinc-100 dark:border-zinc-700 dark:text-neutral-400 dark:hover:bg-zinc-800 transition-colors disabled:opacity-50"
        :disabled="selectedCount === 0 || isMutating"
        @click="$emit('clear')"
      >
        선택 해제
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  selectedCount: number;
  allChecked: boolean;
  isMutating: boolean;
}>();

defineEmits<{
  (e: "toggle-all"): void;
  (e: "restore"): void;
  (e: "delete"): void;
  (e: "clear"): void;
}>();
</script>
