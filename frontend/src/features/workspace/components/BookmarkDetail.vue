<template>
  <section class="h-full flex flex-col bg-card text-card-foreground">
    <!-- 헤더 -->
    <header class="sticky top-0 z-10 bg-card border-b border-border">
      <div class="relative flex items-center justify-between px-4 py-3">
        <!-- 좌: 컬렉션 정보 -->
        <div class="min-w-0">
          <p
            v-if="collectionName"
            class="text-[15px] font-semibold text-neutral-500 dark:text-neutral-400 truncate"
          >
            {{ collectionName }}
          </p>
        </div>

        <!-- 우: 액션(보기/편집) -->
        <div class="flex items-center gap-1">
          <!-- 보기 모드 -->
          <template v-if="!isEditing">
            <button
              type="button"
              @click="handleEdit"
              class="inline-flex items-center px-2.5 py-1.5 rounded-md hover:bg-accent text-sm"
            >
              <EditIcon class="size-6" />
              <span class="ml-1">수정</span>
            </button>
            <button
              type="button"
              @click="showDeleteDialog = true"
              class="inline-flex items-center px-3 py-1.5 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 text-sm"
            >
              <TrashIcon class="size-6" />
              <span class="ml-1">삭제</span>
            </button>
          </template>

          <!-- 편집 모드 -->
          <template v-else>
            <button
              type="button"
              @click="handleCancel"
              class="inline-flex items-center px-2.5 py-1.5 rounded-md hover:bg-accent text-sm"
            >
              <CloseIcon class="size-6" />
              <span class="ml-1">취소</span>
            </button>
            <button
              type="button"
              :disabled="!canSave"
              @click="handleSave"
              class="inline-flex items-center px-3 py-1.5 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              <SaveIcon class="size-6" />
              <span class="ml-1">저장</span>
            </button>
          </template>

          <!-- 우측 상단: 닫기 -->
          <button
            type="button"
            class="ml-1 p-2 rounded-md hover:bg-accent"
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
            ref="titleRef"
            v-model="editedBookmark!.title"
            type="text"
            class="w-full border-0 border-b border-border/70 bg-transparent px-3 py-2.5 text-xl font-semibold placeholder:text-muted-foreground/60 focus:border-ring focus:outline-none focus:ring-1 focus:ring-ring/40"
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
        <template v-if="isEditing">
          <label class="block text-sm text-muted-foreground">링크 *</label>
          <input
            v-model="editedBookmark!.url"
            type="url"
            class="w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
            placeholder="https://example.com"
          />
          <p
            v-if="editedBookmark!.url && !isUrlValid"
            class="text-xs text-red-500 mt-1"
          >
            올바른 URL 형식이 아닙니다.
          </p>
        </template>
        <template v-else>
          <label class="block text-sm text-muted-foreground">링크</label>
          <div class="border rounded-lg p-4 bg-muted/30">
            <div class="flex items-center justify-between gap-3 text-sm">
              <div class="min-w-0 flex items-center gap-2">
                <ExternalLinkIcon class="size-4 shrink-0" />
                <a
                  :href="currentBookmark.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="text-primary hover:underline truncate"
                  :title="currentBookmark.url"
                >
                  {{ currentBookmark.url }}
                </a>
              </div>
              <button
                type="button"
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
            class="min-h-[120px] resize-y w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
            placeholder="(북마크에 대한 설명을 남겨보세요.)"
          />
        </template>
        <template v-else>
          <p class="text-muted-foreground whitespace-pre-wrap min-h-[120px]">
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
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import TrashIcon from "@/components/icons/TrashIcon.vue";
import SaveIcon from "@/components/icons/SaveIcon.vue";
import CloseIcon from "@/components/icons/CloseIcon.vue";
import EditIcon from "@/components/icons/EditIcon.vue";

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

defineExpose({ focusTitle });

// 편집 상태
const isEditing = ref(false);
const editedBookmark = ref<Bookmark | null>(null);
const showDeleteDialog = ref(false);
const confirmBtnRef = ref<HTMLElement | null>(null);
const titleRef = ref<HTMLInputElement | null>(null);

const currentBookmark = computed<Bookmark>(
  () => (isEditing.value ? editedBookmark.value : props.bookmark) as Bookmark
);

const isUrlValid = computed(() => {
  const v = (editedBookmark.value?.url ?? "").trim();
  if (!isEditing.value) return false;
  try {
    new URL(v);
    return true;
  } catch {
    return false;
  }
});
const canSave = computed(
  () => isEditing.value && !!editedBookmark.value && isUrlValid.value
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
function focusTitle() {
  titleRef.value?.focus();
}
function handleEdit() {
  editedBookmark.value = { ...props.bookmark };
  isEditing.value = true;
}
function handleCancel() {
  editedBookmark.value = null;
  isEditing.value = false;
}
function handleSave() {
  if (!canSave.value || !editedBookmark.value) return;
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
