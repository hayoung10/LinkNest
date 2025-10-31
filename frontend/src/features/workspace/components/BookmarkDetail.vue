<template>
  <section class="h-full flex flex-col bg-card text-card-foreground">
    <!-- 헤더 -->
    <header class="sticky top-0 z-10 bg-card border-b border-border">
      <div class="flex items-center justify-between px-4 py-3">
        <div class="min-w-0">
          <p
            v-if="collectionName"
            class="text-xs text-muted-foreground truncate"
          >
            {{ collectionName }}
          </p>
        </div>

        <div class="flex items-center gap-1">
          <!-- 보기/편집 모드 -->
          <template v-if="!isEditing">
            <button
              @click="handleEdit"
              class="inline-flex items-center px-2.5 py-1.5 rounded-md hover:bg-accent text-sm"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="size-6"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M12 20h9" />
                <path
                  d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"
                />
              </svg>

              <span>수정</span>
            </button>
            <button
              @click="showDeleteDialog = true"
              class="inline-flex items-center px-3 py-1.5 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 text-sm"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="size-6"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M3 6h18" />
                <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                <path d="M10 11v6M14 11v6" />
              </svg>

              <span>삭제</span>
            </button>
          </template>
          <template v-else>
            <button
              @click="handleCancel"
              class="inline-flex items-center px-2.5 py-1.5 rounded-md hover:bg-accent text-sm"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="size-6"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>

              <span>취소</span>
            </button>
            <button
              @click="handleSave"
              class="inline-flex items-center px-3 py-1.5 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 text-sm"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="size-6"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path
                  d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"
                />
                <path d="M17 21v-8H7v8" />
                <path d="M7 3v5h8" />
              </svg>

              <span>저장</span>
            </button>
          </template>

          <!-- 공통: 닫기 -->
          <button
            type="button"
            class="p-2 rounded-md hover:bg-accent"
            aria-label="패널 닫기"
            @click="$emit('close')"
          >
            ✕
          </button>
        </div>
      </div>
    </header>

    <!-- 본문 -->
    <div class="flex-1 overflow-auto px-5 py-4 space-y-6">
      <!-- 제목 -->
      <div class="space-y-2">
        <template v-if="isEditing">
          <input
            v-model="editedBookmark!.title"
            type="text"
            class="w-full rounded-md px-3 py-2 text-xl font-semibold bg-transparent border-0 border-b border-border/70 focus:border-ring focus:outline-none focus:ring-0 placeholder:text-muted-foreground/70"
            placeholder="(제목 없음)"
          />
        </template>
        <template v-else>
          <h1 class="text-xl font-semibold text-foreground truncate">
            {{ displayTitle(currentBookmark) }}
          </h1>
        </template>
      </div>

      <!-- 링크 -->
      <div class="space-y-2">
        <label class="block text-sm text-muted-foreground">링크</label>
        <template v-if="isEditing">
          <input
            v-model="editedBookmark!.url"
            type="url"
            class="w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
            placeholder="https://example.com"
          />
        </template>
        <template v-else>
          <div class="border rounded-lg p-4 bg-muted/30">
            <div class="flex items-center justify-between gap-3 text-sm">
              <div class="min-w-0 flex items-center gap-2">
                <svg
                  class="size-4 shrink-0"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    d="M18 13v6a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"
                  />
                  <path d="M15 3h6v6" />
                  <path d="M10 14L21 3" />
                </svg>
                <a
                  :href="currentBookmark.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="text-primary hover:underline truncate"
                >
                  {{ currentBookmark.url }}
                </a>
              </div>
              <button
                class="text-xs px-2.5 py-1.5 rounded-md hover:bg-accent shrink-0"
                @click="openUrl"
              >
                방문
              </button>
            </div>
          </div>
        </template>
      </div>

      <!-- 설명 -->
      <div class="space-y-2">
        <label class="block text-sm text-muted-foreground">설명</label>
        <template v-if="isEditing">
          <textarea
            v-model="editedBookmark!.description"
            class="w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
            placeholder="(북마크에 대한 설명을 남겨보세요.)"
          />
        </template>
        <template v-else>
          <p class="text-muted-foreground whitespace-pre-wrap min-h-[1.5rem]">
            {{
              currentBookmark.description ??
              "(북마크에 대한 설명을 남겨보세요.)"
            }}
          </p>
        </template>
      </div>
    </div>
  </section>

  <!-- 삭제 확인 다이얼로그 -->
  <teleport to="#modals">
    <div
      v-if="showDeleteDialog"
      class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
      @click.self="showDeleteDialog = false"
      @keydown.esc="showDeleteDialog = false"
    >
      <div
        class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6"
        role="dialog"
        aria-modal="true"
        aria-labelledby="bookmark-delete-title"
      >
        <header class="mb-4">
          <h3
            id="bookmark-delete-title"
            class="text-[17px] font-semibold leading-6"
          >
            북마크 삭제
          </h3>
          <p class="mt-1 text-sm text-muted-foreground">
            정말로 이 북마크를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.
          </p>
        </header>
        <footer class="mt-6 flex justify-end gap-2">
          <button
            type="button"
            class="px-4 py-2 border rounded-md text-sm hover:bg-accent"
            @click="showDeleteDialog = false"
          >
            취소
          </button>
          <button
            ref="confirmBtnRef"
            type="button"
            class="px-4 py-2 rounded-md text-sm bg-red-600 text-white hover:bg-red-500"
            @click="handleDelete"
          >
            삭제
          </button>
        </footer>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import type { Bookmark, ID } from "@/types/common";

const props = defineProps<{
  bookmark: Bookmark;
  collectionName?: string;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (
    e: "update-bookmark",
    payload: {
      id: ID;
      title?: string | null;
      url: string;
      description?: string | null;
    }
  ): void;
  (e: "delete-bookmark", id: ID): void;
}>();

// 편집 상태
const isEditing = ref(false);
const editedBookmark = ref<Bookmark | null>(null);
const showDeleteDialog = ref(false);
const confirmBtnRef = ref<HTMLElement | null>(null);

const currentBookmark = computed<Bookmark>(
  () => (isEditing.value ? editedBookmark.value : props.bookmark) as Bookmark
);

// 유틸
function displayTitle(b: Bookmark) {
  const t = (b.title ?? "").trim();
  return t || "(제목 없음)";
}
const normalize = (s?: string | null) => {
  const v = (s ?? "").trim();
  return v ? v : null;
};

// 핸들러
function handleEdit() {
  editedBookmark.value = { ...props.bookmark };
  isEditing.value = true;
}
function handleCancel() {
  editedBookmark.value = null;
  isEditing.value = false;
}
function handleSave() {
  if (!editedBookmark.value) return;
  emit("update-bookmark", {
    id: editedBookmark.value.id,
    title: normalize(editedBookmark.value.title),
    url: editedBookmark.value.url.trim(),
    description: normalize(editedBookmark.value.description),
  });
  isEditing.value = false;
}
function handleDelete() {
  emit("delete-bookmark", props.bookmark.id);
  showDeleteDialog.value = false;
}
function openUrl() {
  const url = currentBookmark.value?.url;
  if (url) window.open(url, "_blank", "noopener,noreferrer");
}

watch(showDeleteDialog, async (open) => {
  if (open) {
    await nextTick();
    confirmBtnRef.value?.focus();
  }
});
</script>
