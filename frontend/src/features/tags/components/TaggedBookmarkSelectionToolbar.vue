<template>
  <div
    v-if="selectedCount > 0"
    class="mb-3 flex flex-wrap items-center justify-between gap-2 rounded-xl border border-border bg-card/60 px-3 py-2"
  >
    <!-- 왼쪽: 전체 선택 -->
    <button
      type="button"
      class="text-sm px-3 py-2 rounded-md border border-border hover:bg-accent transition-colors"
      @click="$emit('toggle-all')"
    >
      {{ allChecked ? "전체 해제" : "전체 선택" }}
    </button>

    <!-- 오른쪽: 선택 정보 + 액션 -->
    <div class="flex flex-wrap items-center gap-2">
      <div class="text-sm text-muted-foreground">
        선택됨: {{ selectedCount }}개
      </div>

      <button
        type="button"
        class="text-sm px-3 py-2 rounded-md border transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-transparent"
        :class="
          selectedCount > 0 && !isTagMutating
            ? 'border-red-300 bg-red-50 text-red-600 hover:bg-red-100'
            : 'border-border hover:bg-accent'
        "
        :disabled="selectedCount === 0 || isTagMutating"
        @click="$emit('detach')"
      >
        태그에서 제거
      </button>

      <div class="flex flex-wrap items-center gap-2">
        <select
          class="h-9 rounded-md px-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 focus:outline-none focus:ring-2 focus:ring-violet-500/30 disabled:opacity-50"
          :value="String(targetTagId ?? '')"
          :disabled="isTagMutating || candidates.length === 0"
          @change="
            $emit(
              'update:targetTagId',
              toId(($event.target as HTMLSelectElement).value),
            )
          "
        >
          <option value="" disabled>교체할 태그</option>
          <option v-for="t in candidates" :key="t.id" :value="String(t.id)">
            {{ t.name }} ({{ t.bookmarkCount }})
          </option>
        </select>

        <button
          type="button"
          class="text-sm px-3 py-2 rounded-md border transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-transparent"
          :class="
            canReplace && !isTagMutating
              ? 'border-blue-300 bg-blue-50 text-blue-600 hover:bg-blue-100'
              : 'border-border hover:bg-accent'
          "
          :disabled="isTagMutating || !canReplace"
          @click="$emit('replace')"
        >
          교체 적용
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ID } from "@/types/common";

type CandidateTag = {
  id: ID;
  name: string;
  bookmarkCount: number;
};

const props = defineProps<{
  selectedCount: number;
  allChecked: boolean;

  isTagMutating: boolean;

  candidates: CandidateTag[];
  targetTagId: ID | "" | null;

  canReplace: boolean;
}>();

defineEmits<{
  (e: "toggle-all"): void;
  (e: "detach"): void;
  (e: "replace"): void;
  (e: "update:targetTagId", v: ID | ""): void;
}>();

function toId(v: string): ID | "" {
  if (!v) return "";
  return Number(v) as ID;
}
</script>
