<template>
  <aside
    id="tag-sidebar"
    v-bind="$attrs"
    class="relative border-r border-border flex flex-col bg-card text-card-foreground"
  >
    <!-- 상단 영역 -->
    <header
      class="flex items-center justify-between px-4 h-14 border-b border-border"
    >
      <div class="min-w-0">
        <div class="text-sm font-semibold truncate">태그</div>
      </div>

      <button
        type="button"
        class="inline-flex items-center justify-center size-7 rounded-md border border-border transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-50 disabled:cursor-not-allowed"
        aria-label="새 태그 만들기"
        title="새 태그"
        :disabled="isLoading || hasError"
        @click="openAddTagDialog"
      >
        <PlusIcon :size="16" />
      </button>
    </header>

    <!-- 필터 영역 (정렬 + 검색) -->
    <div class="p-3 space-y-2 border-b border-border">
      <!-- 정렬 -->
      <div class="relative">
        <label class="sr-only" for="tag-sort">태그 정렬</label>
        <select
          id="tag-sort"
          v-model="sort"
          class="w-full appearance-none [background-image:none] rounded-md pl-3 pr-9 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60 disabled:opacity-50"
          :disabled="isLoading || hasError"
        >
          <option v-for="o in sortOptions" :key="o.value" :value="o.value">
            {{ o.label }}
          </option>
        </select>

        <span
          class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground"
        >
          <ChevronIcon :size="16" direction="down" />
        </span>
      </div>

      <!-- 검색 -->
      <input
        v-model="qInput"
        type="text"
        class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
        placeholder="태그 검색 (2글자 이상)"
        :disabled="hasError"
        @compositionstart="onCompositionStart"
        @compositionend="onCompositionEnd"
      />
      <span
        v-if="isReloading"
        class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground"
        aria-hidden="true"
      >
        <span
          class="inline-block size-4 rounded full border-2 border-current border-t-transparent animate-spin"
        />
      </span>
    </div>

    <!-- 리스트 -->
    <nav
      ref="listWrapRef"
      class="flex-1 min-h-0 overflow-y-auto p-2 text-sm flex flex-col"
    >
      <!-- 헤더 라벨 -->
      <div
        class="sticky top-0 z-10 flex items-center justify-between px-2 py-2 bg-card/95 backdrop-blur border-b border-border/60"
      >
        <div
          class="text-xs font-medium text-muted-foreground uppercase tracking-wide"
        >
          Tags
        </div>
        <div class="text-xs text-muted-foreground tabular-nums">
          {{ items.length }}
        </div>
      </div>

      <!-- 상태 분기 -->
      <BaseError
        v-if="hasError"
        title="태그를 불러올 수 없습니다"
        :description="errorMessage"
        :onRetry="onRetry"
      />

      <BaseLoading v-else-if="isInitialLoading" label="태그를 불러오는 중…" />

      <BaseEmpty
        v-else-if="isEmpty"
        title="태그가 없습니다."
        description="우측 상단 '+' 버튼으로 새 태그를 추가해보세요."
      />

      <ul v-else class="space-y-1">
        <li v-for="t in items" :key="t.id">
          <button
            type="button"
            class="w-full inline-flex items-center justify-between gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-accent/70 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/40"
            :class="
              t.id === selectedTagId
                ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-100 ring-1 ring-blue-300'
                : 'text-foreground hover:bg-zinc-100 dark:hover:bg-zinc-800'
            "
            @click="$emit('select-tag', t.id)"
          >
            <!-- 태그 이름 -->
            <span class="min-w-0 truncate">{{ t.name }}</span>

            <!-- 북마크 수 -->
            <span
              class="shrink-0 text-[11px] tabular-nums px-2 py-0.5 rounded-full bg-zinc-100 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-200"
              >{{ t.bookmarkCount }}</span
            >
          </button>
        </li>
      </ul>

      <div class="h-2" />

      <!-- 더 보기 -->
      <div
        class="sticky bottom-0 mt-auto py-1 flex justify-center bg-card/90 backdrop-blur border-t border-border"
      >
        <span v-if="isLoadingMore" class="text-xs text-muted-foreground"
          >불러오는 중…</span
        >
        <span v-else-if="isEndReached" class="text-xs text-muted-foreground"
          >마지막입니다</span
        >
        <span
          v-else-if="tagsStore.hasNext"
          class="text-xs text-muted-foreground"
          >더 보기</span
        >
      </div>

      <!-- 무한 스크롤 -->
      <div
        v-if="canLoadMore"
        ref="sentinelRef"
        class="h-8"
        aria-hidden="true"
      />
    </nav>

    <!-- 새 태그 추가 다이얼로그 -->
    <teleport to="#modals">
      <div
        v-if="showAddDialog"
        class="fixed inset-0 z-50 bg-black/40 grid place-items-center p-4"
        @click.self="showAddDialog = false"
        @keydown.esc="showAddDialog = false"
      >
        <form
          class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6 relative"
          role="dialog"
          aria-modal="true"
          aria-labelledby="tag-create"
          @submit.prevent="handleCreate"
        >
          <header class="mb-4">
            <h3 id="tag-create" class="text-[17px] font-semibold leading-6">
              새 태그 추가
            </h3>
            <p class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              북마크에 사용할 태그를 만들어보세요.
            </p>
          </header>

          <div class="my-4 h-px bg-zinc-200/80 dark:bg-zinc-700/60"></div>

          <div class="space-y-2">
            <label class="block text-sm">태그 이름 *</label>
            <input
              ref="createInputRef"
              v-model="createName"
              type="text"
              class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
              placeholder="태그 이름"
              required
            />
          </div>

          <footer class="mt-6 flex justify-end gap-2">
            <button
              type="button"
              class="px-4 py-2 rounded-md text-sm border border-zinc-300/70 dark:border-zinc-600/60 bg-zinc-100/70 dark:bg-zinc-800/70 hover:bg-zinc-100 dark:hover:bg-zinc-800"
              @click="showAddDialog = false"
            >
              취소
            </button>
            <button
              type="submit"
              :disabled="!canCreate || isCreating"
              class="px-4 py-2 rounded-md text-sm bg-zinc-900 text-white hover:bg-zinc-800 focus:outline-none focus:ring-2 focus:ring-zinc-900/30 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              추가
            </button>
          </footer>
        </form>
      </div>
    </teleport>
  </aside>
</template>

<script setup lang="ts">
import {
  computed,
  onBeforeUnmount,
  onMounted,
  nextTick,
  ref,
  watch,
} from "vue";
import { storeToRefs } from "pinia";
import type { ID } from "@/types/common";
import type { TagSort } from "@/api/tags";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";
import { useTagsStore } from "@/stores/tags";
import { useToastStore } from "@/stores/toast";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import ChevronIcon from "@/components/icons/ChevronIcon.vue";
import { useInfiniteScroll } from "@/composables/useInfiniteScroll";
import { getErrorMessage } from "@/api/errors";

defineOptions({ name: "TagSidebar" });

const props = defineProps<{ selectedTagId: ID | null }>();
const emit = defineEmits<{ (e: "select-tag", id: ID): void }>();

const tagsStore = useTagsStore();
const toast = useToastStore();

const { items, q, sort, isLoading, error, loaded } = storeToRefs(tagsStore);

const sortOptions: Array<{ value: TagSort; label: string }> = [
  { value: "NAME_ASC", label: "이름순" },
  { value: "NEWEST", label: "최신순" },
  { value: "OLDEST", label: "오래된순" },
  { value: "COUNT_DESC", label: "북마크순" },
];

const hasError = computed(() => !!error.value);
const isInitialLoading = computed(
  () => isLoading.value && items.value.length === 0,
);
const isEmpty = computed(() => loaded.value && items.value.length === 0);

const errorMessage = computed(() => {
  if (!error.value) return undefined;
  return "네트워크 상태를 확인한 뒤 다시 시도해주세요.";
});

const loadedCount = computed(() => items.value.length);

// ------------------------
// Search
// ------------------------
const qInput = ref(q.value ?? "");

let qTimer: ReturnType<typeof setTimeout> | null = null;

function clearTimer() {
  if (!qTimer) return;
  clearTimeout(qTimer);
  qTimer = null;
}

function normalizeQuery(raw: string) {
  return (raw ?? "").trim().normalize("NFC");
}

async function runSearch(raw: string) {
  const nextQ = normalizeQuery(raw);

  // 2글자 미만: 검색 X
  if (nextQ.length > 0 && nextQ.length < 2) return;

  cleanup();
  tagsStore.setQuery({ q: nextQ, page: 0 });

  await tagsStore.safeReload();
  await nextTick();
  scrollToTop();
  setup();
}

function scheduleSearch(raw: string) {
  clearTimer();

  qTimer = setTimeout(async () => {
    if (isComposing.value) return;

    runSearch(raw).catch(() => {});
  }, 250);
}

const isComposing = ref(false);
const isReloading = computed(
  () => isLoading.value && items.value.length > 0 && tagsStore.page === 0,
);

function onCompositionStart() {
  isComposing.value = true;
}

function onCompositionEnd(e: CompositionEvent) {
  isComposing.value = false;
  scheduleSearch(qInput.value);
}

function scrollToTop() {
  const el = listWrapRef.value;
  if (!el) return;
  el.scrollTo({ top: 0, behavior: "auto" });
}

onBeforeUnmount(() => {
  cleanup();
  clearTimer();
});

// ------------------------
// Actions
// ------------------------
async function onRetry() {
  try {
    await tagsStore.safeReload();
  } catch {
    toast.error("태그를 불러오지 못했습니다.");
  }
}

onMounted(async () => {
  try {
    await tagsStore.load();
  } catch {}
  await nextTick();
  setup();
});

// ------------------------
// 더 보기
// ------------------------
const listWrapRef = ref<HTMLElement | null>(null);
const sentinelRef = ref<HTMLElement | null>(null);

const canLoadMore = computed(
  () => loaded.value && tagsStore.hasNext && !isLoading.value,
);
const isLoadingMore = computed(() => isLoading.value && items.value.length > 0);
const isEndReached = computed(() => loaded.value && !tagsStore.hasNext);

async function loadMore() {
  if (!canLoadMore.value) return;

  const prevPage = tagsStore.page;

  const moved = tagsStore.nextPage();
  if (!moved) return;

  try {
    await tagsStore.load(true);
  } catch {
    tagsStore.setQuery({ page: prevPage });
  }
}

const { setup, cleanup } = useInfiniteScroll(
  listWrapRef,
  sentinelRef,
  loadMore,
  { rootMargin: "200px", threshold: 0 },
);

// ------------------------
// Watchers
// ------------------------
watch(
  () => q.value,
  (next) => {
    const v = next ?? "";
    if (qInput.value === v) return;
    qInput.value = v;
  },
);

watch(
  () => qInput.value,
  (next) => {
    if (isComposing.value) return;
    scheduleSearch(next);
  },
);

watch(
  () => sort.value,
  async (next, prev) => {
    if (next === prev) return;

    cleanup();
    tagsStore.setQuery({ sort: next, page: 0 });

    try {
      await tagsStore.safeReload();
      await nextTick();

      scrollToTop();
      setup();
    } catch {
      toast.error("정렬을 적용하지 못했습니다.");
    }
  },
);

// ------------------------
// Dialog
// ------------------------
const showAddDialog = ref(false);

const createInputRef = ref<HTMLInputElement | null>(null);
const createName = ref("");
const canCreate = computed(() => createName.value.trim().length > 0);

const isCreating = ref(false);

function openAddTagDialog() {
  if (isLoading.value || hasError.value) return;

  createName.value = "";
  showAddDialog.value = true;

  nextTick(() => {
    createInputRef.value?.focus();
    createInputRef.value?.select();
  });
}

async function handleCreate() {
  if (!canCreate.value || isCreating.value) return;

  isCreating.value = true;
  try {
    await tagsStore.create({ name: createName.value.trim() });
    showAddDialog.value = false;
  } catch (e) {
    toast.error(getErrorMessage(e, "태그 추가에 실패했습니다."));
  } finally {
    isCreating.value = false;
  }
}
</script>
