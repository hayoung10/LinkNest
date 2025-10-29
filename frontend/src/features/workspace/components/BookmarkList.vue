<template>
  <main class="flex-1 overflow-auto bg-background">
    <!-- 선택 없음 -->
    <div
      v-if="!hasSelection"
      class="h-full min-h-[60vh] grid place-items-center text-muted/70 py-16"
    >
      <div class="text-center space-y-2">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="size-14 mx-auto opacity-30"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
        >
          <path
            d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
          />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10 9 9 9 8 9" />
        </svg>
        <p>컬렉션을 선택해주세요.</p>
      </div>
    </div>

    <!-- 컬렉션 선택됨 -->
    <div v-else class="max-w-4xl mx-auto p-8 animate-fadeIn">
      <!-- 헤더 -->
      <header class="flex items-center justify-between mb-2">
        <div class="min-w-0 flex items-center gap-2 pl-3">
          <svg
            class="size-7 text-muted-foreground opacity-80"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <path d="M3 7h5l2 2h11v9" />
            <path d="M3 7v10a2 2 0 0 0 2 2h16" />
          </svg>
          <h2 class="text-xl font-semibold text-foreground truncate">
            {{ collection?.name }}
          </h2>
        </div>

        <div class="flex text-center gap-2 pr-3">
          <button
            type="button"
            class="inline-flex items-center gap-1 px-3.5 py-1.5 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 transition-colors"
            aria-label="새 북마크 추가"
            @click="openAddDialog"
          >
            <svg
              viewBox="0 0 24 24"
              class="size-4"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              aria-hidden="true"
            >
              <path d="M12 5v14" />
              <path d="M5 12h14" />
            </svg>
            <span>추가</span>
          </button>
        </div>
      </header>

      <div class="divider" />

      <!-- 북마크 리스트 -->
      <template v-if="bookmarks.length">
        <ul class="mt-0" role="list" aria-label="북마크 목록">
          <template v-for="b in bookmarks" :key="b.id">
            <li
              class="px-2 py-3 rounded-md hover:bg-accent cursor-pointer select-none"
            >
              <button
                type="button"
                class="w-full text-left px-3"
                @click="onSelect(b)"
                :title="displayTitle(b)"
              >
                <div class="text-sm font-semibold flex items-center gap-2">
                  <span class="truncate">{{ b.title }}</span>

                  <!-- 링크 아이콘 -->
                  <a
                    :href="b.url"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="p-1 text-neutral-400 hover:text-neutral-700 dark:hover:text-neutral-200 transition-colors"
                    aria-label="링크 새 탭에서 열기"
                    @click.stop
                  >
                    <svg
                      viewBox="0 0 24 24"
                      class="size-4"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <path
                        d="M18 13v6a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"
                      />
                      <polyline points="15 3 21 3 21 9" />
                      <line x1="10" y1="14" x2="21" y2="3" />
                    </svg>
                  </a>
                </div>
                <div
                  class="mt-1 text-xs text-neutral-500 dark:text-neutral-400 flex items-center gap-2"
                >
                  <span class="truncate">{{ domain(b.url) }}</span>
                  <span aria-hidden="true">·</span>
                  <time :datetime="b.createdAt || ''">
                    {{ formatDate(b.createdAt) }}
                  </time>
                </div>
              </button>
            </li>

            <div class="divider mx-6" />
          </template>
        </ul>

        <footer
          class="mt-2 text-xs text-neutral-500 dark:text-neutral-400 text-center select-none"
        >
          {{ bookmarks.length }} 북마크
        </footer>
      </template>

      <!-- 빈 컬렉션 -->
      <div v-else class="text-sm text-muted-foreground py-12">
        <div class="flex flex-col items-center gap-3">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="size-12 opacity-30"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <path d="M3 7h18" />
            <path d="M6 7V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2" />
            <path d="M19 7v12a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V7" />
            <path d="M12 11v6" />
            <path d="M9 14h6" />
          </svg>
          <p class="text-center">링크를 추가하세요.</p>
        </div>
      </div>
    </div>
  </main>

  <!-- 새 북마크 추가 다이얼로그 -->
  <teleport to="#modals">
    <div
      v-if="showAddDialog"
      class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
      @click.self="closeAddDialog"
      @keydown.esc="closeAddDialog"
    >
      <form
        class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6 relative"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="dialogTitleId"
        @submit.prevent="handleAdd"
      >
        <header class="mb-4">
          <h3 :id="dialogTitleId" class="text-[17px] font-semibold leading-6">
            새 북마크 추가
          </h3>
          <p class="mt-1 text-sm text-muted-foreground">
            저장하고 싶은 링크의 정보를 입력해주세요.
          </p>
        </header>

        <div class="my-4 h-px bg-zinc-200/80 dark:bg-zinc-700/60"></div>

        <div class="space-y-2">
          <label class="block text-sm" :for="titleInputId">제목</label>
          <input
            :id="titleInputId"
            ref="titleInputRef"
            v-model.trim="form.title"
            type="text"
            class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
            placeholder="(제목 없음)"
          />
          <label class="block text-sm" :for="urlInputId">링크 *</label>
          <input
            :id="urlInputId"
            v-model.trim="form.url"
            type="url"
            class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
            placeholder="http://example.com"
            required
          />
          <label class="block text-sm" :for="descInputId">설명</label>
          <input
            :id="descInputId"
            v-model.trim="form.description"
            type="text"
            class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
            placeholder="(북마크에 대한 설명을 남겨보세요.)"
          />
        </div>

        <footer class="mt-6 flex justify-end gap-2">
          <button
            type="button"
            class="px-4 py-2 rounded-md text-sm border border-zinc-300/70 dark:border-zinc-600/60 bg-zinc-100/70 dark:bg-zinc-800/70 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            @click="closeAddDialog"
          >
            취소
          </button>
          <button
            type="submit"
            :disabled="!canSubmit"
            :aria-disabled="!canSubmit"
            class="px-4 py-2 rounded-md text-sm bg-zinc-900 text-white dark:bg-zinc-100 dark:text-zinc-900 hover:bg-zinc-800 dark:hover:bg-zinc-200"
          >
            추가
          </button>
        </footer>
      </form>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import type { Bookmark, Collection, ID } from "@/types/common";

const props = defineProps<{ collection: Collection | null }>();

type NewBookmarkPayload = {
  url: string;
  collectionId: ID; // number
  title?: string | null;
  description?: string | null;
};

const emit = defineEmits<{
  (e: "add-bookmark", bookmark: NewBookmarkPayload): void;
  (e: "select-bookmark", bookmark: Bookmark): void;
}>();

const hasSelection = computed(() => !!props.collection);
const bookmarks = computed<Bookmark[]>(() => props.collection?.bookmarks ?? []);

// 다이얼로그
const showAddDialog = ref(false);
const form = ref({ title: "", url: "", description: "" });

const dialogTitleId = "add-bookmark-title";
const titleInputId = "add-bookmark-title-input";
const urlInputId = "add-bookmark-url-input";
const descInputId = "add-bookmark-desc-input";
const titleInputRef = ref<HTMLInputElement | null>(null);

const canSubmit = computed(() => form.value.url.trim().length > 0);

function openAddDialog() {
  resetForm();
  showAddDialog.value = true;
}
function closeAddDialog() {
  resetForm();
  showAddDialog.value = false;
}
function resetForm() {
  form.value.title = "";
  form.value.url = "";
  form.value.description = "";
}
function handleAdd() {
  if (!canSubmit.value || !props.collection?.id) return;

  const payload: NewBookmarkPayload = {
    url: form.value.url.trim(),
    collectionId: props.collection.id,
    title: form.value.title.trim() || undefined,
    description: form.value.description.trim() || undefined,
  };

  emit("add-bookmark", payload);
  closeAddDialog();
}
watch(showAddDialog, async (open) => {
  if (open) {
    await nextTick();
    titleInputRef.value?.focus();
  }
});

// 유틸
function displayTitle(b: Bookmark): string {
  const t = (b.title ?? "").trim();
  return t || "(제목 없음)";
}
function domain(url: string) {
  try {
    return new URL(url).host.replace(/^www\./, "");
  } catch {
    return url;
  }
}
function formatDate(iso?: string): string {
  if (!iso) return "-";
  try {
    return new Intl.DateTimeFormat("ko-KR", {
      year: "numeric",
      month: "numeric",
      day: "numeric",
    }).format(new Date(iso));
  } catch {
    return "-";
  }
}
function onSelect(b: Bookmark) {
  emit("select-bookmark", b);
}
</script>

<style scoped>
.divider {
  height: 1px;
  margin: 6px 4px;
  background: color-mix(in oklab, currentColor 12%, transparent);
  border-radius: 1px;
}
</style>
