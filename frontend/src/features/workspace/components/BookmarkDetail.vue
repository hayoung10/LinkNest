<template>
  <section class="h-full flex flex-col bg-card text-card-foreground">
    <!-- 헤더 -->
    <header class="sticky top-0 z-10 bg-card border-b border-border">
      <div class="relative flex items-center justify-between px-4 py-3">
        <!-- 좌: 컬렉션 정보 -->
        <div class="min-w-0">
          <p
            v-if="collectionName"
            class="text-[15px] font-semibold text-foreground truncate"
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
              :disabled="isBookmarkMutating"
              @click="handleEdit"
              class="inline-flex items-center px-2.5 py-1.5 rounded-md hover:bg-accent text-sm"
            >
              <EditIcon class="size-6" />
              <span class="ml-1">수정</span>
            </button>
            <button
              type="button"
              :disabled="isBookmarkMutating"
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
              :disabled="isBookmarkMutating"
              @click="handleCancel"
              class="inline-flex items-center px-2.5 py-1.5 rounded-md hover:bg-accent text-sm"
            >
              <CloseIcon class="size-6" />
              <span class="ml-1">취소</span>
            </button>
            <button
              type="button"
              :disabled="isBookmarkMutating || !canSave"
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
      <!-- 커버 이미지 -->
      <section class="rounded-2xl border border-border/60 bg-muted/20 p-4">
        <div class="flex items-start gap-4">
          <!-- 좌: 미리보기 -->
          <div class="shrink-0">
            <div
              class="relative h-20 w-20 rounded-xl overflow-hidden bg-zinc-200/70 dark:bg-zinc-800/70 border border-border/60"
            >
              <img
                v-if="coverPreviewUrl"
                :src="coverPreviewUrl"
                alt="커버 이미지 미리보기"
                class="h-full w-full object-cover"
                draggable="false"
              />
              <div
                v-else
                class="h-full w-full grid place-items-center text-zinc-500/70 dark:text-zinc-400/60"
              >
                <span class="text-xs select-none"> 없음 </span>
              </div>
            </div>
          </div>

          <!-- 우: 설정 -->
          <div class="min-w-0 flex-1">
            <div class="flex items-center justify-between gap-3">
              <p class="text-sm font-semibold text-foreground">커버 이미지</p>

              <!-- 모드 선택 -->
              <div
                class="shrink-0 inline-flex rounded-lg border border-border/60 bg-background overflow-hidden"
              >
                <button
                  type="button"
                  :disabled="isCoverMutating || isEditing"
                  class="px-3 py-1.5 text-xs transition-colors"
                  :class="
                    hasCustomCover
                      ? 'bg-zinc-200 dark:bg-zinc-800 text-foreground'
                      : 'text-muted-foreground hover:bg-zinc-100 dark:hover:bg-zinc-900/40'
                  "
                  @click="setCoverMode('AUTO')"
                >
                  AUTO
                </button>
                <button
                  type="button"
                  :disabled="isCoverMutating || isEditing"
                  class="px-3 py-1.5 text-xs transition-colors border-l border-border/60"
                  :class="
                    currentBookmark.imageMode === 'CUSTOM'
                      ? 'bg-zinc-200 dark:bg-zinc-800 text-foreground'
                      : 'text-muted-foreground hover:bg-zinc-100 dark:hover:bg-zinc-900/40'
                  "
                  @click="onClickChangeCover"
                >
                  CUSTOM
                </button>
                <button
                  type="button"
                  :disabled="isCoverMutating || isEditing"
                  class="px-3 py-1.5 text-xs transition-colors border-l border-border/60"
                  :class="
                    currentBookmark.imageMode === 'NONE'
                      ? 'bg-zinc-200 dark:bg-zinc-800 text-foreground'
                      : 'text-muted-foreground hover:bg-zinc-100 dark:hover:bg-zinc-900/40'
                  "
                  @click="setCoverMode('NONE')"
                >
                  NONE
                </button>
              </div>
            </div>

            <!-- 커스텀 업로드/삭제 -->
            <div class="mt-3 flex flex-wrap items-center gap-2">
              <input
                ref="coverInputRef"
                type="file"
                accept="image/*"
                class="hidden"
                @change="onCoverFileChange"
              />
              <template v-if="currentBookmark.imageMode === 'CUSTOM'">
                <button
                  type="button"
                  :disabled="isCoverMutating || isEditing"
                  class="inline-flex items-center h-9 px-3 rounded-md text-sm border border-border/60 hover:bg-zinc-100 dark:hover:bg-zinc-900/40 transition-colors"
                  @click="onClickChangeCover"
                >
                  사진 변경
                </button>
                <button
                  v-if="currentBookmark.customImageUrl"
                  type="button"
                  :disabled="isCoverMutating || isEditing"
                  class="inline-flex items-center h-9 px-3 rounded-md text-sm text-red-600/80 hover:text-red-700 dark:text-red-400/80 dark:hover:text-red-300 border border-border/60 hover:bg-zinc-100 dark:hover:bg-zinc-900/40 transition-colors"
                  @click="removeCustomCover"
                >
                  사진 삭제
                </button>
              </template>
            </div>
          </div>
        </div>
      </section>

      <!-- 제목 -->
      <div class="space-y-2">
        <!-- 이모지 -->
        <div
          v-if="isEditing || currentBookmark.emoji"
          class="relative flex items-end gap-2"
        >
          <button
            ref="emojiTriggerEl"
            type="button"
            :class="[
              'relative flex items-center justify-center size-9 rounded-md transition-colors',
              isEditing && currentBookmark.emoji
                ? 'hover:bg-zinc-200 dark:hover:bg-zinc-800 cursor-pointer'
                : 'cursor-default',
            ]"
            @click="onEmojiStateClick"
            :aria-label="currentBookmark.emoji ? '이모지 변경' : '이모지 상태'"
            title="이모지"
          >
            <span v-if="currentBookmark.emoji" class="text-2xl leading-none">{{
              currentBookmark.emoji
            }}</span>
            <span v-else class="text-lg leading-none opacity-20">+</span>
          </button>

          <template v-if="isEditing">
            <button
              v-if="!currentBookmark.emoji"
              type="button"
              class="mb-0.5 rounded-md px-2 py-1 text-xs leading-none text-muted-foreground/60 hover:bg-zinc-200 dark:hover:bg-zinc-800 hover:text-foreground transition-colors"
              @click="toggleEmojiPicker"
            >
              이모지 추가
            </button>

            <button
              v-else
              type="button"
              class="mb-0.5 rounded-md px-2 py-1 text-xs leading-none text-red-600/80 hover:text-red-700 dark:text-red-400/80 dark:hover:text-red-300 hover:bg-zinc-200 dark:hover:bg-zinc-800 transition-colors"
              @click="removeEmoji"
            >
              이모지 제거
            </button>
          </template>

          <!-- EmojiPicker popover -->
          <div
            v-if="emojiPickerOpen"
            ref="emojiPopoverRef"
            class="absolute left-0 top-full mt-2 z-50"
          >
            <EmojiPicker :native="true" @select="onEmojiSelected" />
          </div>
        </div>

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
          <h1
            class="text-xl font-semibold truncate"
            :class="
              hasTitle
                ? 'text-foreground'
                : 'text-neutral-400 dark:text-neutral-500'
            "
          >
            {{ displayTitle(currentBookmark) }}
          </h1>
        </template>
      </div>

      <!-- 링크 -->
      <div class="space-y-2">
        <template v-if="isEditing">
          <label class="block text-sm text-foreground">링크 *</label>
          <input
            ref="urlRef"
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
          <label class="block text-sm text-foreground">링크</label>
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
                :disabled="isBookmarkMutating"
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
        <label class="block text-sm text-foreground">설명</label>
        <div class="h-px w-full bg-neutral-200 dark:bg-neutral-800 mt-4"></div>
        <template v-if="isEditing">
          <textarea
            v-model="editedBookmark!.description"
            class="min-h-[120px] resize-y w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
            placeholder="(북마크에 대한 설명을 남겨보세요.)"
          />
        </template>
        <template v-else>
          <p
            class="whitespace-pre-wrap min-h-[120px]"
            :class="
              hasDescription
                ? 'text-foreground'
                : 'text-neutral-400 dark:text-neutral-500'
            "
          >
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
            :disabled="isMutating.deleteBookmark"
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
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import type { Bookmark, ID, ImageMode } from "@/types/common";
import * as BookmarkApi from "@/api/bookmarks";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import TrashIcon from "@/components/icons/TrashIcon.vue";
import SaveIcon from "@/components/icons/SaveIcon.vue";
import CloseIcon from "@/components/icons/CloseIcon.vue";
import EditIcon from "@/components/icons/EditIcon.vue";
import EmojiPicker from "vue3-emoji-picker";
import "vue3-emoji-picker/css";
import { storeToRefs } from "pinia";
import { useWorkspaceStore } from "@/stores/workspace";
import { useToastStore } from "@/stores/toast";

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
      emoji?: string | null;
    }
  ): void;
  (e: "delete-bookmark", id: ID): void;

  (e: "replace-bookmark", bookmark: Bookmark): void;
}>();

const toast = useToastStore();
const workspace = useWorkspaceStore();

const { isMutating } = storeToRefs(workspace);

const isModeUpdating = ref(false);

const isBookmarkMutating = computed(
  () => isMutating.value.updateBookmark || isMutating.value.deleteBookmark
);
const isCoverMutating = computed(
  () => isBookmarkMutating.value || isModeUpdating.value
);

defineExpose({ focusUrl });

// 편집 상태
const isEditing = ref(false);
const editedBookmark = ref<Bookmark | null>(null);
const showDeleteDialog = ref(false);
const confirmBtnRef = ref<HTMLElement | null>(null);
const urlRef = ref<HTMLInputElement | null>(null);

const currentBookmark = computed<Bookmark>(() => {
  return isEditing.value && editedBookmark.value
    ? editedBookmark.value
    : props.bookmark;
});

const hasTitle = computed(() => !!currentBookmark.value.title?.trim());
const hasDescription = computed(
  () => !!currentBookmark.value.description?.trim()
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
function focusUrl() {
  urlRef.value?.focus();
}
async function handleEdit() {
  editedBookmark.value = { ...props.bookmark };
  isEditing.value = true;
  closeEmojiPicker();

  await nextTick();
  focusUrl();
}
function handleCancel() {
  editedBookmark.value = null;
  isEditing.value = false;
  closeEmojiPicker();
}
function handleSave() {
  if (!canSave.value || !editedBookmark.value) return;
  emit("update-bookmark", {
    id: editedBookmark.value.id,
    title: normalize(editedBookmark.value.title),
    url: editedBookmark.value.url.trim(),
    description: normalize(editedBookmark.value.description),
    emoji: editedBookmark.value.emoji ?? null,
  });
  isEditing.value = false;
  editedBookmark.value = null;
  closeEmojiPicker();
}
function handleDelete() {
  emit("delete-bookmark", props.bookmark.id);
  showDeleteDialog.value = false;
}
function openUrl() {
  const url = currentBookmark.value?.url;
  if (url) window.open(url, "_blank", "noopener,noreferrer");
}

// ------------------------
// Emoji Picker
// ------------------------
const emojiPickerOpen = ref(false);
const emojiPopoverRef = ref<HTMLElement | null>(null);
const emojiTriggerEl = ref<HTMLElement | null>(null);

function closeEmojiPicker() {
  emojiPickerOpen.value = false;
}
function toggleEmojiPicker() {
  if (!isEditing.value) return;
  emojiPickerOpen.value = !emojiPickerOpen.value;
}
function onEmojiStateClick() {
  if (!isEditing.value) return;
  if (!currentBookmark.value.emoji) return;
  toggleEmojiPicker();
}
function onEmojiSelected(emoji: any) {
  const picked = emoji?.i ?? null;
  if (!picked) return;
  if (!editedBookmark.value) return;

  editedBookmark.value.emoji = picked;
  closeEmojiPicker();
}
function removeEmoji() {
  if (!editedBookmark.value) return;
  editedBookmark.value.emoji = null;
  closeEmojiPicker();
}

function onDocPointerDown(e: PointerEvent) {
  if (!emojiPickerOpen.value) return;

  const target = e.target as Node | null;
  if (!target) return;

  if (emojiPopoverRef.value?.contains(target)) return;
  if (emojiTriggerEl.value?.contains(target)) return;

  closeEmojiPicker();
}
function onDocKeyDown(e: KeyboardEvent) {
  if (!emojiPickerOpen.value) return;
  if (e.key === "Escape") {
    e.preventDefault();
    closeEmojiPicker();
  }
}

// ------------------------
// Cover
// ------------------------
const coverInputRef = ref<HTMLInputElement | null>(null);

const coverPreviewUrl = computed(() => {
  const b = currentBookmark.value;
  if (!b) return null;

  if (b.imageMode === "CUSTOM") return b.customImageUrl ?? null;
  if (b.imageMode === "AUTO") return b.autoImageUrl ?? null;
  return null;
});

const hasCustomCover = computed(() => {
  const b = currentBookmark.value;
  return b.imageMode === "CUSTOM" && !!b.customImageUrl;
});

async function setCoverMode(mode: Exclude<ImageMode, "CUSTOM">) {
  if (isModeUpdating.value) return;
  if (currentBookmark.value.imageMode === mode) return;

  try {
    isModeUpdating.value = true;

    const updated = await BookmarkApi.updateImageMode(props.bookmark.id, {
      imageMode: mode,
    });
    emit("replace-bookmark", updated);
  } catch (e) {
    console.error("ImageMode 업데이트 실패:", e);
    toast.error("커버 설정 변경에 실패했습니다.");
  } finally {
    isModeUpdating.value = false;
  }
}

function onClickChangeCover() {
  coverInputRef.value?.click();
}
async function onCoverFileChange(event: Event) {
  if (isCoverMutating.value) return;

  const input = event.target as HTMLInputElement | null;
  const file = input?.files?.[0] ?? null;
  if (!file) return;

  try {
    const updated = await BookmarkApi.uploadCover(props.bookmark.id, file);
    updated.imageMode = "CUSTOM";
    emit("replace-bookmark", updated);
  } catch (e) {
    console.error("커버 이미지 업로드 실패:", e);
    toast.error("커버 이미지 업로드에 실패했습니다.");
  } finally {
    if (input) input.value = "";
  }
}
async function removeCustomCover() {
  if (isCoverMutating.value) return;
  if (!currentBookmark.value.customImageUrl) return;

  try {
    const updated = await BookmarkApi.removeCover(props.bookmark.id);
    emit("replace-bookmark", updated);
  } catch (e) {
    console.error("커버 이미지 삭제 실패:", e);
    toast.error("커버 이미지 삭제에 실패했습니다.");
  }
}

// ------------------------
onMounted(() => {
  document.addEventListener("pointerdown", onDocPointerDown, true);
  document.addEventListener("keydown", onDocKeyDown);
});
onUnmounted(() => {
  document.removeEventListener("pointerdown", onDocPointerDown, true);
  document.removeEventListener("keydown", onDocKeyDown);
});

watch(showDeleteDialog, async (open) => {
  if (open) {
    await nextTick();
    confirmBtnRef.value?.focus();
  }
});
</script>
