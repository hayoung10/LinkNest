<template>
  <div
    v-if="selectedCount > 0"
    class="mb-3 flex flex-col gap-3 rounded-xl border border-border bg-card/60 p-3 md:flex-row md:items-center md:justify-between md:gap-4"
  >
    <!-- 모바일: 전체 선택 + 선택 개수 + 태그 해제 -->
    <div class="flex items-center gap-2">
      <button
        type="button"
        class="shrink-0 rounded-md border border-border px-2.5 py-1.5 text-sm transition-colors hover:bg-accent sm:px-3 sm:py-2"
        @click="$emit('toggle-all')"
      >
        {{ allChecked ? "전체 해제" : "전체 선택" }}
      </button>

      <div class="shrink-0 text-xs text-muted-foreground sm:text-sm">
        선택됨: {{ selectedCount }}개
      </div>

      <button
        type="button"
        class="shrink-0 rounded-md border px-2.5 py-1.5 text-sm transition-colors disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-transparent sm:px-3 sm:py-2 md:hidden"
        :class="
          selectedCount > 0 && !isTagMutating
            ? 'border-red-300 bg-red-50 text-red-600 hover:bg-red-100'
            : 'border-border hover:bg-accent'
        "
        :disabled="selectedCount === 0 || isTagMutating"
        @click="$emit('detach')"
      >
        태그 해제
      </button>
    </div>

    <!-- 데스트톱: 태그 헤제 + 태그 교체 작업을 오른쪽에 배치 -->
    <div
      class="flex min-w-0 flex-col gap-2 md:flex-row md:items-center md:justify-end"
    >
      <button
        type="button"
        class="shrink-0 rounded-md border px-2.5 py-1.5 text-sm transition-colors disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-transparent sm:px-3 sm:py-2 hidden md:inline-flex"
        :class="
          selectedCount > 0 && !isTagMutating
            ? 'border-red-300 bg-red-50 text-red-600 hover:bg-red-100'
            : 'border-border hover:bg-accent'
        "
        :disabled="selectedCount === 0 || isTagMutating"
        @click="$emit('detach')"
      >
        태그 해제
      </button>

      <!-- 교체할 태그 선택 + 교체 적용 버튼 -->
      <div class="flex min-w-0 items-center gap-2 sm:justify-end">
        <select
          class="h-9 min-w-0 flex-1 rounded-md border border-zinc-300/70 bg-zinc-100 px-2 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/30 disabled:opacity-50 dark:bg-zinc-800 dark:border-zinc-600/60 md:flex-initial"
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
          class="shrink-0 rounded-md border px-3 py-1.5 text-sm transition-colors disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-transparent sm:py-2"
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
