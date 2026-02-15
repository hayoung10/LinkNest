<template>
  <header class="mb-2">
    <div class="flex items-center justify-between">
      <div class="min-w-0 pl-3">
        <!-- path -->
        <template v-if="showPath">
          <div
            class="mb-1 ml-0.5 min-h-[16px] text-xs text-zinc-500 flex items-center gap-1 min-w-0"
          >
            <template v-if="isLoadingPath">
              <span
                class="inline-block h-3 w-28 rounded bg-zinc-200/70 animate-pulse"
              />
            </template>

            <template v-else-if="cPath.length">
              <template v-for="(p, idx) in cPath" :key="p.id">
                <span class="truncate">
                  <span v-if="p.emoji" class="mr-1">{{ p.emoji }}</span>
                  {{ p.name }}
                </span>
                <span v-if="idx < cPath.length - 1" class="opacity-60">/</span>
              </template>
            </template>
          </div>
        </template>

        <!-- 높이 맞춤 -->
        <template v-else>
          <div
            class="mb-1 ml-0.5 min-h-[16px] text-xs text-zinc-500"
            aria-hidden="true"
          />
        </template>

        <!-- 제목 -->
        <div class="min-w-0 flex items-center gap-2">
          <slot name="title">
            <span class="shrink-0 text-muted-foreground opacity-80">
              <template v-if="collection?.emoji">
                <span class="text-[20px] leading-none">{{
                  collection.emoji
                }}</span>
              </template>
              <template v-else>
                <FolderIcon size="22" />
              </template>
            </span>

            <!-- 컬렉션 이름 -->
            <h2 class="text-xl font-semibold text-foreground truncate">
              {{ collection?.name ?? "컬렉션" }}
            </h2>
          </slot>
        </div>
      </div>

      <div class="flex text-center gap-2 pr-3">
        <template v-if="showAdd">
          <button
            type="button"
            :disabled="isAddDisabled"
            class="inline-flex items-center gap-1 px-3.5 py-1.5 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-neutral-900"
            aria-label="새 북마크 추가"
            @click="$emit('open-add')"
          >
            <PlusIcon :size="16" klass="shrink-0" />
            <span>추가</span>
          </button>
        </template>

        <!-- 높이/정렬 맞춤 -->
        <template v-else>
          <span
            class="inline-flex items-center px-3.5 py-1.5 opacity-0 select-none"
            aria-hidden="true"
          >
            <span class="inline-block size-9" />
            <span>추가</span>
          </span>
        </template>
      </div>
    </div>

    <!-- 검색 -->
    <div class="pl-3 pr-3 mt-3">
      <div class="relative">
        <SearchIcon
          :size="16"
          class="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400"
        />
        <Input
          v-model="qInput"
          placeholder="북마크 검색 (Enter로 실행)"
          class="pl-9 pr-9"
          @keydown="onKeyDown"
          @compositionstart="onCompositionStart"
          @compositionend="onCompositionEnd"
        />
        <button
          v-if="
            qInput.trim().length > 0 || workspace.bookmarksQ.trim().length > 0
          "
          type="button"
          class="absolute right-2.5 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600"
          aria-label="검색어 지우기"
          @click="clearSearch"
        >
          <CloseIcon :size="16" />
        </button>
      </div>

      <!-- 2글자 미만 안내 -->
      <p
        v-if="qInput.trim().length > 0 && qInput.trim().length < 2"
        class="mt-1 text-xs text-zinc-500"
      >
        검색어는 2글자 이상 입력해 주세요.
      </p>
    </div>
  </header>
</template>

<script setup lang="ts">
import { CollectionPathRes } from "@/api/types";
import CloseIcon from "@/components/icons/CloseIcon.vue";
import FolderIcon from "@/components/icons/FolderIcon.vue";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import SearchIcon from "@/components/icons/SearchIcon.vue";
import Input from "@/components/ui/Input.vue";
import { useWorkspaceStore } from "@/stores/workspace";
import { CollectionNode } from "@/types/common";
import { computed, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    collection: CollectionNode | null;
    cPath: CollectionPathRes[];
    isLoadingPath: boolean;
    isAddDisabled: boolean;

    showPath?: boolean;
    showAdd?: boolean;
  }>(),
  {
    showPath: true,
    showAdd: true,
  },
);

const emit = defineEmits<{
  (e: "open-add"): void;
  (e: "searched"): void;
  (e: "clear-focus"): void;
}>();

const workspace = useWorkspaceStore();

const q = computed(() => workspace.bookmarksQ);
const qInput = ref(q.value ?? "");

const isComposing = ref(false);

function normalizeQuery(raw: string) {
  return (raw ?? "").trim().normalize("NFC");
}

async function runSearch() {
  const nextQ = normalizeQuery(qInput.value);

  // 2글자 미만: 검색 X
  if (nextQ.length > 0 && nextQ.length < 2) return;

  workspace.setBookmarksQuery({ bookmarksQ: nextQ });
  await workspace.reloadBookmarks();
  emit("searched");
}

function onKeyDown(e: KeyboardEvent) {
  if (e.key !== "Enter") return;
  if (isComposing.value) return;
  runSearch().catch(() => {});
}

function onCompositionStart() {
  isComposing.value = true;
}

function onCompositionEnd() {
  isComposing.value = false;
}

async function clearSearch() {
  qInput.value = "";
  workspace.resetBookmarksQuery();
  await workspace.reloadBookmarks();
  emit("clear-focus");
  emit("searched");
}

watch(
  () => q.value,
  (next) => {
    const v = next ?? "";
    if (qInput.value === v) return;
    qInput.value = v;
  },
);
</script>
