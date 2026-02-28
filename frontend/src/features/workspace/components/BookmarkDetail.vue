<template>
  <section class="h-full flex flex-col bg-card text-card-foreground">
    <!-- 헤더 -->
    <header class="sticky top-0 z-10 bg-card border-b border-border">
      <!-- 1행 (좌: 닫기 / 우: 액션 + 즐겨찾기) -->
      <div class="flex items-center justify-between px-4 py-2">
        <!-- 좌: 닫기 -->
        <button
          type="button"
          :disabled="isBookmarkMutating"
          class="p-2 rounded-md transition-colors duration-150"
          :class="
            isBookmarkMutating
              ? 'opacity-50 cursor-not-allowed'
              : 'hover:bg-zinc-200/70 dark:hover:bg-zinc-700/60'
          "
          aria-label="패널 닫기"
          @click="$emit('close')"
        >
          <CloseIcon class="size-5 text-muted-foreground" />
        </button>

        <!-- 우: 액션(보기/편집) -->
        <div class="flex items-center gap-1">
          <!-- 보기 모드 -->
          <template v-if="!isEditing">
            <button
              type="button"
              :disabled="isBookmarkMutating"
              @click="handleEdit"
              class="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-md text-sm text-muted-foreground transition-colors duration-150 hover:bg-black/10 hover:text-foreground disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <EditIcon class="size-5" />
              <span>수정</span>
            </button>

            <button
              type="button"
              :disabled="isBookmarkMutating || isFavoriteMutating"
              class="p-2 rounded-md text-muted-foreground transition-colors duration-150 hover:bg-black/10 hover:text-foreground disabled:opacity-50 disabled:cursor-not-allowed"
              aria-label="즐겨찾기"
              @click="onToggleFavorite"
            >
              <StarIcon
                :filled="props.bookmark.isFavorite"
                :klass="
                  props.bookmark.isFavorite
                    ? 'text-amber-400'
                    : 'text-muted-foreground'
                "
                size="18"
              />
            </button>
          </template>

          <!-- 편집 모드 -->
          <template v-else>
            <button
              type="button"
              :disabled="isBookmarkMutating"
              @click="handleCancel"
              class="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-md text-sm text-muted-foreground transition-colors duration-150 hover:bg-black/10 hover:text-foreground disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <CloseIcon class="size-5" />
              <span>취소</span>
            </button>

            <button
              type="button"
              :disabled="isBookmarkMutating || !canSave"
              @click="handleSave"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm bg-neutral-900 text-white transition-colors duration-150 hover:bg-black/80 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <SaveIcon class="size-5" />
              <span>저장</span>
            </button>
          </template>
        </div>
      </div>

      <!-- 2행 (컬렉션 이름 + (이모지 + 제목) + 최근 수정일) -->
      <div class="px-5 pb-3">
        <p
          v-if="collectionName"
          class="ml-1 mb-2 flex items-center gap-1.5 truncate text-[13px] leading-5 text-muted-foreground"
        >
          <span v-if="collectionEmoji" class="shrink-0 text-sm leading-none">
            {{ collectionEmoji }}
          </span>
          <FolderIcon v-else class="shrink-0 size-3.5 opacity-60" />
          <span class="truncate">{{ collectionName }}</span>
        </p>

        <div class="flex items-end justify-between gap-3">
          <!-- 좌: 이모지 + 제목 -->
          <div class="min-w-0 flex items-center gap-3">
            <!-- 이모지 + popover -->
            <div class="relative shrink-0">
              <button
                ref="emojiTriggerEl"
                type="button"
                :class="[
                  'flex items-center justify-center size-9 rounded-md transition-colors',
                  isEditing
                    ? 'hover:bg-zinc-200 dark:hover:bg-zinc-800 cursor-pointer'
                    : 'cursor-default',
                ]"
                @click="onEmojiTriggerClick"
                :aria-label="
                  currentBookmark.emoji ? '이모지 변경' : '이모지 상태'
                "
                title="이모지"
              >
                <span
                  v-if="currentBookmark.emoji"
                  class="text-2xl leading-none"
                  >{{ currentBookmark.emoji }}</span
                >
                <span
                  v-else-if="isEditing"
                  class="text-lg leading-none opacity-20"
                  >+</span
                >
                <span
                  v-else
                  class="text-xl leading-none text-zinc-400/60 dark:text-zinc-500/60 select-none"
                  aria-hidden="true"
                >
                  •
                </span>
              </button>

              <!-- EmojiPicker popover -->
              <div
                v-if="emojiPickerOpen"
                ref="emojiPopoverRef"
                class="absolute left-0 top-full mt-2 z-50"
              >
                <EmojiPicker :native="true" @select="onEmojiSelected" />

                <button
                  v-if="isEditing && editedBookmark?.emoji"
                  type="button"
                  class="mt-2 w-full rounded-lg border border-border/70 bg-background px-3 py-2 text-sm text-red-600 shadow-[0_10px_30px_rgba(0,0,0,0.20)] hover:bg-red-50 transition-colors dark:text-red-400 dark:hover:bg-red-950/30"
                  @click="removeEmoji"
                >
                  이모지 제거
                </button>
              </div>
            </div>

            <!-- 제목 -->
            <div class="min-w-0">
              <template v-if="isEditing">
                <input
                  ref="titleRef"
                  v-model="editedBookmark!.title"
                  type="text"
                  class="w-full border-0 border-b border-border/70 bg-transparent px-0 py-1 text-xl font-semibold placeholder:text-muted-foreground/60 focus:border-ring focus:outline-none focus:ring-1 focus:ring-ring/40"
                  placeholder="(제목 없음)"
                />
              </template>
              <template v-else>
                <h1
                  class="text-xl font-semibold truncate leading-none"
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
          </div>

          <!-- 우: (보기 모드) 최근 수정일 -->
          <p
            v-if="!isEditing"
            class="mb-2 mr-1 shrink-0 text-xs text-muted-foreground leading-none"
          >
            {{ updatedAtText }}
          </p>
        </div>
      </div>
    </header>

    <!-- 본문 -->
    <div class="flex-1 overflow-auto px-5 py-4 space-y-6">
      <!-- 커버 이미지 -->
      <section
        class="rounded-2xl border border-border/60 bg-zinc-50/60 dark:bg-zinc-900/20 p-5 relative"
      >
        <div
          v-if="isEditing"
          class="absolute inset-0 rounded-2xl z-10 cursor-not-allowed"
          aria-hidden="true"
        />

        <div class="flex items-start gap-4">
          <!-- 좌: 미리보기 -->
          <div
            class="shrink-0"
            :class="isEditing ? 'opacity-70 grayscale-[0.25] saturate-50' : ''"
          >
            <div
              class="relative h-20 w-20 rounded-xl overflow-hidden bg-zinc-200/70 dark:bg-zinc-800/70 border border-border/60"
            >
              <img
                v-if="coverPreviewUrl"
                :src="coverPreviewUrl"
                class="h-full w-full object-cover transition-opacity duration-300"
              />
              <!-- AUTO 생성 중 -->
              <div
                v-else-if="isAutoGenerating"
                class="h-full bg-zinc-200/60 dark:bg-zinc-800/60"
              />

              <!-- NONE 모드 -->
              <div
                v-else
                class="h-full w-full grid place-items-center text-zinc-500/70 dark:text-zinc-400/60"
              >
                <span class="text-xs select-none">없음</span>
              </div>

              <!-- 스피너 오버레이 -->
              <div
                v-if="isAutoGenerating"
                class="absolute inset-0 bg-black/10 dark:bg-black/20 backdrop-blur-[1px] flex items-center justify-center"
              >
                <div
                  class="w-4 h-4 border-2 border-white/80 border-t-transparent rounded-full animate-spin"
                />
              </div>
            </div>
          </div>

          <!-- 우: 설정 -->
          <div class="min-w-0 flex-1">
            <div
              :class="
                isEditing ? 'opacity-70 grayscale-[0.25] saturate-50' : ''
              "
            >
              <div class="flex items-center justify-between gap-3">
                <p
                  class="text-sm font-semibold text-zinc-700 dark:text-zinc-200"
                >
                  커버 이미지
                </p>

                <!-- 모드 선택 -->
                <div
                  class="shrink-0 inline-flex rounded-lg border border-border/60 bg-background overflow-hidden"
                >
                  <button
                    type="button"
                    :disabled="isCoverMutating || isEditing"
                    class="px-3 py-1.5 text-xs transition-colors"
                    :class="[
                      currentBookmark.imageMode === 'AUTO'
                        ? 'bg-zinc-200 dark:bg-zinc-800 text-foreground'
                        : 'text-muted-foreground hover:bg-zinc-100 dark:hover:bg-zinc-900/40',
                      isCoverMutating || isEditing
                        ? 'opacity-50 cursor-not-allowed'
                        : '',
                    ]"
                    @click="setCoverMode('AUTO')"
                  >
                    AUTO
                  </button>
                  <button
                    type="button"
                    :disabled="isCoverMutating || isEditing"
                    class="px-3 py-1.5 text-xs transition-colors border-l border-border/60"
                    :class="[
                      currentBookmark.imageMode === 'CUSTOM'
                        ? 'bg-zinc-200 dark:bg-zinc-800 text-foreground'
                        : 'text-muted-foreground hover:bg-zinc-100 dark:hover:bg-zinc-900/40',
                      isCoverMutating || isEditing
                        ? 'opacity-50 cursor-not-allowed'
                        : '',
                    ]"
                    @click="onClickChangeCover"
                  >
                    CUSTOM
                  </button>
                  <button
                    type="button"
                    :disabled="isCoverMutating || isEditing"
                    class="px-3 py-1.5 text-xs transition-colors border-l border-border/60"
                    :class="[
                      currentBookmark.imageMode === 'NONE'
                        ? 'bg-zinc-200 dark:bg-zinc-800 text-foreground'
                        : 'text-muted-foreground hover:bg-zinc-100 dark:hover:bg-zinc-900/40',
                      isCoverMutating || isEditing
                        ? 'opacity-50 cursor-not-allowed'
                        : '',
                    ]"
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
                    v-if="hasCustomCover"
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

            <p
              v-if="isEditing"
              class="mt-2 text-xs font-medium text-red-600 dark:text-red-400"
            >
              편집 중에는 커버/모드를 변경할 수 없어요
            </p>
          </div>
        </div>
      </section>

      <div class="space-y-5">
        <!-- 링크 -->
        <section
          class="rounded-2xl border border-border/60 bg-background p-5 border-l-4 border-l-blue-500/60"
        >
          <label
            class="block text-sm font-semibold tracking-wide text-zinc-500 dark:text-zinc-400"
            >링크</label
          >

          <div class="mt-3">
            <template v-if="isEditing">
              <input
                ref="urlRef"
                v-model="editedBookmark!.url"
                type="url"
                :aria-invalid="editedBookmark!.url ? !isUrlValid : false"
                class="w-full rounded-md px-3 py-2 text-sm bg-blue-50/40 dark:bg-blue-950/20 border border-blue-200 dark:border-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-400/40 focus:border-blue-400 placeholder:text-blue-400/70"
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
              <div
                class="rounded-xl border border-border/60 bg-zinc-100/80 dark:bg-zinc-900/50 p-4 transition-colors hover:bg-blue-50/70 dark:hover:bg-blue-950/30"
              >
                <div class="flex items-center justify-between gap-3 text-sm">
                  <div class="min-w-0 flex items-center gap-2">
                    <ExternalLinkIcon
                      class="size-4 shrink-0 text-blue-600 dark:text-blue-400"
                    />
                    <a
                      :href="currentBookmark.url"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="truncate font-medium text-blue-700 dark:text-blue-300 underline-offset-2 hover:underline"
                      :title="currentBookmark.url"
                    >
                      {{ currentBookmark.url }}
                    </a>
                  </div>
                  <button
                    type="button"
                    :disabled="isBookmarkMutating"
                    class="shrink-0 inline-flex items-center gap-1.5 rounded-md px-3 py-1.5 text-xs font-medium bg-blue-600 text-white hover:bg-blue-500 disabled:opacity-50"
                    @click="openUrl"
                  >
                    방문
                  </button>
                </div>
              </div>
            </template>
          </div>
        </section>

        <!-- 태그 -->
        <section class="rounded-2xl border border-border/60 bg-background p-5">
          <label
            class="block text-sm font-semibold tracking-wide text-zinc-500 dark:text-zinc-400"
            >태그</label
          >

          <div class="mt-3">
            <template v-if="isEditing">
              <TagInput
                v-model="editedBookmark!.tags"
                :max="3"
                placeholder="태그 입력 후 Enter로 추가"
                :disabled="isBookmarkMutating"
              />
            </template>

            <template v-else>
              <div
                v-if="(currentBookmark.tags?.length ?? 0) > 0"
                class="flex flex-wrap gap-2"
              >
                <span
                  v-for="t in currentBookmark.tags"
                  :key="t"
                  class="inline-flex items-center rounded-full bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-800 px-3 py-1 text-xs font-medium text-blue-700 dark:text-blue-300"
                  >{{ t }}</span
                >
              </div>
              <p v-else class="text-sm italic text-zinc-400 dark:text-zinc-500">
                (태그 없음)
              </p>
            </template>
          </div>
        </section>

        <!-- 설명 -->
        <section class="rounded-2xl border border-border/60 bg-background p-5">
          <label
            class="block text-sm font-semibold tracking-wide text-zinc-500 dark:text-zinc-400"
            >설명</label
          >
          <div class="mt-3">
            <template v-if="isEditing">
              <textarea
                v-model="editedBookmark!.description"
                class="min-h-[140px] resize-y w-full rounded-md px-3 py-2 text-sm bg-background dark:bg-background border border-border/60 focus:outline-none focus:ring-2 focus:ring-zinc-400/40"
                placeholder="(북마크에 대한 설명을 남겨보세요.)"
              />
            </template>
            <template v-else>
              <p
                class="whitespace-pre-wrap text-sm leading-relaxed min-h-[120px]"
                :class="
                  hasDescription
                    ? 'text-zinc-800 dark:text-zinc-100'
                    : 'italic text-zinc-400 dark:text-zinc-500'
                "
              >
                {{
                  currentBookmark.description ??
                  "(북마크에 대한 설명을 남겨보세요.)"
                }}
              </p>
            </template>
          </div>
        </section>

        <!-- 삭제 버튼 -->
        <div class="pt-1 flex justify-end">
          <button
            type="button"
            :disabled="isBookmarkMutating"
            @click="showDeleteDialog = true"
            class="inline-flex items-center h-9 px-4 rounded-md border border-red-200 dark:border-red-900/50 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors disabled:opacity-50"
          >
            삭제
          </button>
        </div>
      </div>
    </div>

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
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import type { Bookmark, ID, ImageMode } from "@/types/common";
import * as BookmarkApi from "@/api/bookmarks";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import SaveIcon from "@/components/icons/SaveIcon.vue";
import CloseIcon from "@/components/icons/CloseIcon.vue";
import EditIcon from "@/components/icons/EditIcon.vue";
import FolderIcon from "@/components/icons/FolderIcon.vue";
import EmojiPicker from "vue3-emoji-picker";
import "vue3-emoji-picker/css";
import { storeToRefs } from "pinia";
import { useWorkspaceStore } from "@/stores/workspace";
import { useToastStore } from "@/stores/toast";
import StarIcon from "@/components/icons/StarIcon.vue";
import TagInput from "./TagInput.vue";
import { refreshBookmarkAutoImage } from "@/utils/refreshAutoImage";

const props = defineProps<{
  bookmark: Bookmark;
  collectionName?: string;
  collectionEmoji?: string | null;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "unfavorite", id: ID): void;
  (
    e: "update-bookmark",
    payload: {
      id: ID;
      title: string;
      url: string;
      description: string;
      emoji?: string | null;
      tags: string[];
    },
  ): void;
  (e: "delete-bookmark", id: ID): void;

  (e: "replace-bookmark", bookmark: Bookmark): void;
}>();

const toast = useToastStore();
const workspace = useWorkspaceStore();

const { isMutating } = storeToRefs(workspace);

const isModeUpdating = ref(false);

const isBookmarkMutating = computed(
  () => isMutating.value.updateBookmark || isMutating.value.deleteBookmark,
);
const isCoverMutating = computed(
  () => isBookmarkMutating.value || isModeUpdating.value,
);

const isFavoriteMutating = computed(
  () => isMutating.value.toggleBookmarkFavorite,
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
  () => !!currentBookmark.value.description?.trim(),
);

const updatedAtText = computed(() => {
  const iso = currentBookmark.value.updatedAt;
  if (!iso) return "";

  return (
    new Intl.DateTimeFormat("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    }).format(new Date(iso)) + " 편집"
  );
});

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
  () => isEditing.value && !!editedBookmark.value && isUrlValid.value,
);

function displayTitle(b: Bookmark) {
  const t = (b.title ?? "").trim();
  return t || "(제목 없음)";
}

function focusUrl() {
  urlRef.value?.focus();
}
async function handleEdit() {
  editedBookmark.value = {
    ...props.bookmark,
    tags: props.bookmark.tags ? [...props.bookmark.tags] : [],
  };
  isEditing.value = true;
  closeEmojiPicker();

  await nextTick();
  focusUrl();
}
async function onToggleFavorite() {
  if (isEditing.value) return;

  const prevFavorite = props.bookmark.isFavorite;

  try {
    await workspace.toggleBookmarkFavorite(props.bookmark);

    if (workspace.viewMode === "favorites" && prevFavorite) {
      emit("unfavorite", props.bookmark.id);
    }
  } catch (e) {
    toast.error("즐겨찾기 변경에 실패했습니다.");
  }
}
function handleCancel() {
  editedBookmark.value = null;
  isEditing.value = false;
  closeEmojiPicker();
}
function handleSave() {
  if (!canSave.value || !editedBookmark.value) return;

  const title = (editedBookmark.value.title ?? "").trim();
  const description = (editedBookmark.value.description ?? "").trim();
  const tags = editedBookmark.value.tags ? [...editedBookmark.value.tags] : [];

  emit("update-bookmark", {
    id: editedBookmark.value.id,
    title,
    url: editedBookmark.value.url.trim(),
    description,
    emoji: editedBookmark.value.emoji ?? null,
    tags,
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
function onEmojiTriggerClick() {
  if (!isEditing.value) return;
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

const isAutoGenerating = computed(() => {
  const b = currentBookmark.value;
  return b.imageMode === "AUTO" && !b.autoImageUrl?.trim();
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

    if (updated.imageMode === "AUTO" && !updated.autoImageUrl?.trim()) {
      refreshBookmarkAutoImage(updated.id, BookmarkApi.getBookmark, (latest) =>
        emit("replace-bookmark", latest),
      );
    }
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

  // 파일 타입 체크
  if (!file.type.startsWith("image/")) {
    toast.error("이미지 파일만 업로드할 수 있습니다.");
    if (input) input.value = "";
    return;
  }

  // 파일 크기 체크 (최대 5MB)
  const maxSize = 5 * 1024 * 1024;
  if (file.size > maxSize) {
    toast.error("파일이 너무 큽니다. (최대 5MB)");
    if (input) input.value = "";
    return;
  }

  try {
    const updated = await BookmarkApi.uploadCover(props.bookmark.id, file);
    updated.imageMode = "CUSTOM";
    emit("replace-bookmark", updated);
    toast.success("커버 이미지가 변경되었습니다.");
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

    if (updated.imageMode === "AUTO" && !updated.autoImageUrl?.trim()) {
      refreshBookmarkAutoImage(updated.id, BookmarkApi.getBookmark, (latest) =>
        emit("replace-bookmark", latest),
      );
    }
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
