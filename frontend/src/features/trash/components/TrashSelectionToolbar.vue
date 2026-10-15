<template>
  <div
    v-if="selectedCount > 0"
    class="grid grid-cols-[auto_1fr] items-center gap-x-3 gap-y-3 rounded-xl border border-border bg-background/95 px-3 py-3 backdrop-blur sm:flex sm:justify-between sm:gap-2 sm:py-2"
  >
    <!-- 왼쪽: 전체 선택 -->
    <button
      type="button"
      class="w-fit shrink-0 rounded-md border border-zinc-300 px-3 py-2 text-sm text-foreground hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
      @click="$emit('toggle-all')"
    >
      {{ allChecked ? "전체 해제" : "전체 선택" }}
    </button>

    <!-- 선택 개수: 모바일 첫 번째 줄 오른쪽 -->
    <div
      class="justify-self-end text-right text-sm text-neutral-500 dark:text-neutral-400 sm:order-2 sm:ml-auto"
    >
      {{ selectedCount }}개 선택됨
    </div>

    <!-- 액션 버튼: 모바일 두 번째 줄 전체 너비 -->
    <div
      class="col-span-2 grid w-full grid-cols-3 gap-2 sm:col-span-1 sm:order-3 sm:flex sm:w-auto sm:items-center"
    >
      <button
        type="button"
        class="min-w-0 whitespace-nowrap rounded-md border px-2 py-2 text-sm transition-colors disabled:cursor-not-allowed disabled:opacity-50 sm:px-3"
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
        class="min-w-0 whitespace-nowrap rounded-md border px-2 py-2 text-sm transition-colors disabled:cursor-not-allowed disabled:opacity-50 sm:px-3"
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
        class="min-w-0 whitespace-nowrap rounded-md border border-zinc-200 px-2 py-2 text-sm text-neutral-500 transition-colors hover:bg-zinc-100 disabled:opacity-50 dark:border-zinc-700 dark:text-neutral-400 dark:hover:bg-zinc-800 sm:px-3"
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
