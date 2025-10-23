<template>
  <main class="flex-1 overflow-auto bg-background">
    <!-- 선택 없음 -->
    <div
      v-if="!hasSelection"
      class="h-full grid place-items-center text-muted-foreground"
    >
      <div class="text-center space-y-2">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="size-12 mx-auto opacity-30"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path
            d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
          />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10 9 9 9 8 9" />
        </svg>
        <p>북마크를 선택해주세요</p>
      </div>
    </div>

    <!-- 북마크 선택됨 -->
    <div v-else class="max-w-4xl mx-auto p-8 animate-fadeIn">
      <!-- 헤더 -->
      <header class="flex items-center justify-between mb-6">
        <div class="min-w-0">
          <p v-if="collectionName" class="text-sm text-muted-foreground">
            {{ collectionName }}
          </p>
          <h1 class="mt-1 text-2xl font-semibold truncate">
            {{ bookmark!.title }}
          </h1>
        </div>

        <!-- 보기 모드 액션 -->
        <div v-if="!isEditing" class="flex text-center gap-2">
          <button
            class="px-3 py-1.5 rounded-md hover:bg-accent transition-colors"
            @click="handleEdit"
          >
            <svg
              class="mr-2 size-4 inline-block align-[-2px]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
            >
              <path d="M12 20h9" />
              <path
                d="M16.5 3.5a2.121 2.121 0 1 1 3 3L7 19l-4 1 1-4 12.5-12.5z"
              />
            </svg>
            수정
          </button>
          <button
            class="px-3 py-1.5 rounded-md hover:bg-accent transition-colors"
            @click="showDeleteDialog = true"
          >
            <svg
              class="mr-2 size-4 inline-block align-[-2px]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
            >
              <path d="M3 6h18" />
              <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
              <path d="M10 11v6M14 11v6" />
            </svg>
            삭제
          </button>
        </div>

        <!-- 편집 모드 액션 -->
        <div v-else class="flex items-center gap-2">
          <button
            class="px-3 py-1.5 rounded-md hover:bg-accent transition-colors"
            @click="handleCancel"
          >
            <svg
              class="mr-2 size-4 inline-block align-[-2px]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
            >
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
            취소
          </button>
          <button
            class="px-3 py-1.5 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 transition-colors"
            @click="handleSave"
          >
            <svg
              class="mr-2 size-4 inline-block align-[-2px]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
            >
              <path
                d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2Z"
              />
              <path d="M17 21v-8H7v8" />
              <path d="M7 3v5h8" />
            </svg>
            저장
          </button>
        </div>
      </header>

      <!-- 본문 -->
      <div class="space-y-6">
        <!-- 편집 모드 -->
        <template v-if="isEditing">
          <div class="space-y-2">
            <label class="block text-sm">제목</label>
            <input
              v-model="editedBookmark!.title"
              type="text"
              class="w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
              placeholder="북마크 제목"
            />
          </div>

          <div class="space-y-2">
            <label class="block text-sm">링크</label>
            <input
              v-model="editedBookmark!.url"
              type="text"
              class="w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
              placeholder="https://example.com"
            />
          </div>

          <div class="space-y-2">
            <label class="block text-sm">설명</label>
            <textarea
              v-model="editedBookmark!.description"
              type="text"
              class="w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
              placeholder="북마크 설명"
            />
          </div>
        </template>

        <!-- 편집 모드 -->
        <template v-else>
          <div>
            <h2 class="sr-only">제목</h2>
          </div>

          <!-- 링크 카드 -->
          <div class="border rounded-lg p-4 bg-muted/30">
            <div class="flex items-center justify-between gap-4">
              <div class="min-w-0 flex items-center gap-2 text-sm">
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
                class="text-sm px-3 py-1.5 rounded-md hover:bg-accent shrink-0"
                @click="openUrl"
              >
                방문
              </button>
            </div>
          </div>

          <!-- 설명 -->
          <div>
            <h3 class="font-medium mb-2">설명</h3>
            <p class="text-muted-foreground whitespace-pre-wrap">
              {{ currentBookmark.description }}
            </p>
          </div>
        </template>
      </div>
    </div>

    <!-- 삭제 확인 다이얼로그 -->
    <div
      v-if="showDeleteDialog"
      class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
      @click.self="showDeleteDialog = false"
    >
      <div
        class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6 relative"
      >
        <header class="mb-4">
          <h3 class="text-[17px] font-semibold leading-6">북마크 삭제</h3>
          <p class="mt-1 test-sm text-muted-foreground">
            정말로 이 북마크를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.
          </p>
        </header>
        <footer class="mt-6 flex justify-end gap-2">
          <button
            class="px-4 py-2 border rounded-md text-sm hover:bg-accent"
            @click="showDeleteDialog = false"
          >
            취소
          </button>
          <button
            class="px-4 py-2 rounded-md text-sm bg-red-600 text-white hover:bg-red-500"
            @click="handleDelete"
          >
            삭제
          </button>
        </footer>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

type Bookmark = {
  id: string;
  title: string;
  description: string;
  url: string;
  collectionId: string;
};

const props = defineProps<{
  bookmark: Bookmark | null;
  collectionName?: string;
}>();

const emit = defineEmits<{
  (e: "update-bookmark", payload: Bookmark): void;
  (e: "delete-bookmark", id: string): void;
}>();

const hasSelection = computed(() => !!props.bookmark);

// 편집 상태
const isEditing = ref(false);
const editedBookmark = ref<Bookmark | null>(null);
const showDeleteDialog = ref(false);

// 표시할 북마크
const currentBookmark = computed<Bookmark>(
  () => (isEditing.value ? editedBookmark.value : props.bookmark) as Bookmark
);

function handleEdit() {
  if (!props.bookmark) return;
  editedBookmark.value = { ...props.bookmark };
  isEditing.value = true;
}

function handleCancel() {
  editedBookmark.value = null;
  isEditing.value = false;
}

function handleSave() {
  if (!editedBookmark.value) return;
  emit("update-bookmark", editedBookmark.value);
  isEditing.value = false;
}

function handleDelete() {
  if (!props.bookmark) return;
  emit("delete-bookmark", props.bookmark.id);
  showDeleteDialog.value = false;
}

function openUrl() {
  const url = currentBookmark.value?.url;
  if (url) window.open(url, "_blank");
}
</script>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.animate-fadeIn {
  animation: fadeIn 0.25s ease-in-out;
}
.size-4 {
  width: 16px;
  height: 16px;
}
</style>
