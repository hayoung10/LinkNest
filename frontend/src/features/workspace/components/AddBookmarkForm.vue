<template>
  <section class="h-full flex flex-col bg-card text-card-foreground">
    <!-- 헤더 -->
    <header class="sticky top-0 z-10 bg-card border-b border-border">
      <!-- 1행 (좌: 닫기 / 우: 액션)-->
      <div class="flex items-center justify-between px-4 py-2">
        <!-- 좌: 닫기 -->
        <button
          type="button"
          :disabled="isSaving"
          class="p-2 rounded-md transition-colors duration-150"
          :class="
            isSaving
              ? 'opacity-50 cursor-not-allowed'
              : 'hover:bg-zinc-200/70 dark:hover:bg-zinc-700/60'
          "
          aria-label="패널 닫기"
          @click="$emit('close')"
        >
          <CloseIcon class="size-5 text-muted-foreground" />
        </button>

        <!-- 우: 액션(저장) -->
        <div class="flex items-center gap-1">
          <button
            type="button"
            :disabled="isSaving || !canSave"
            @click="handleSubmit"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm bg-neutral-900 text-white transition-colors duration-150 hover:bg-black/80 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <SaveIcon class="size-5" />
            <span class="ml-1">저장</span>
          </button>
        </div>
      </div>

      <!-- 2행 (제목 + 설명 문구) -->
      <div class="px-5 pb-3">
        <h2 class="text-[15px] font-semibold">새 북마크 추가</h2>
        <p class="mt-1 text-sm text-neutral-500 dark:text-neutral-400">
          저장하고 싶은 링크의 정보를 입력해주세요.
        </p>
      </div>
    </header>

    <!-- 본문 -->
    <div class="flex-1 overflow-auto px-5 py-4 space-y-6">
      <!-- 제목 -->
      <div class="space-y-2">
        <div class="flex items-end gap-3">
          <!-- 이모지 -->
          <div class="relative shrink-0">
            <button
              ref="emojiTriggerEl"
              type="button"
              class="flex items-center justify-center size-9 rounded-md transition-colors hover:bg-zinc-200 dark:hover:bg-zinc-800 cursor-pointer"
              @click="toggleEmojiPicker"
              :aria-label="form.emoji ? '이모지 변경' : '이모지 추가'"
              title="이모지"
            >
              <span v-if="form.emoji" class="text-2xl leading-none">{{
                form.emoji
              }}</span>
              <span v-else class="text-lg leading-none opacity-20">+</span>
            </button>

            <div
              v-if="emojiPickerOpen"
              ref="emojiPopoverRef"
              class="absolute left-0 top-full mt-2 z-50"
            >
              <EmojiPicker :native="true" @select="onEmojiSelected" />

              <button
                v-if="form.emoji"
                type="button"
                class="mt-2 w-full rounded-lg border border-border/70 bg-background px-3 py-2 text-sm text-red-600 shadow-[0_10px_30px_rgba(0,0,0,0.20)] hover:bg-red-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed dark:text-red-400 dark:hover:bg-red-950/30"
                @click="removeEmoji"
              >
                이모지 제거
              </button>
            </div>
          </div>

          <!-- 제목 입력 -->
          <div class="min-w-0 flex-1">
            <input
              :id="titleId"
              v-model="form.title"
              type="text"
              class="w-full border-0 border-b border-border/70 bg-transparent px-3 py-2.5 text-xl font-semibold placeholder:text-muted-foreground/60 focus:border-ring focus:outline-none focus:ring-1 focus:ring-ring/40"
              placeholder="(제목 없음)"
            />
          </div>
        </div>
      </div>

      <!-- 링크 -->
      <div class="space-y-2">
        <label class="block text-sm text-muted-foreground" :for="urlId"
          >링크 *</label
        >
        <input
          :id="urlId"
          ref="urlRef"
          v-model="form.url"
          type="url"
          class="w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
          placeholder="example.com"
          :aria-invalid="form.url ? !isUrlValid : false"
        />

        <!-- 커버 이미지 토글 -->
        <div class="flex items-center justify-between mt-2">
          <label
            for="auto-cover"
            class="text-sm text-neutral-500 dark:text-neutral-400 select-none cursor-pointer"
          >
            커버 이미지 자동 가져오기
          </label>

          <button
            id="auto-cover"
            type="button"
            role="switch"
            :aria-checked="form.imageMode === 'AUTO'"
            class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors"
            :class="
              form.imageMode === 'AUTO'
                ? 'bg-neutral-900 dark:bg-neutral-100'
                : 'bg-zinc-300 dark:bg-zinc-700'
            "
            @click="toggleAutoCover"
          >
            <span
              class="inline-block h-5 w-5 transform rounded-full bg-white dark:bg-zinc-900 transition-transform"
              :class="
                form.imageMode === 'AUTO' ? 'translate-x-5' : 'translate-x-1'
              "
            />
          </button>
        </div>

        <p v-if="form.url && !isUrlValid" class="text-xs text-red-500 mt-1">
          올바른 URL 형식이 아닙니다.
        </p>
      </div>

      <!-- 태그 -->
      <div class="space-y-2">
        <label class="block text-sm text-muted-foreground">태그</label>

        <TagInput
          v-model="form.tags"
          :max="3"
          placeholder="태그 입력 후 Enter로 추가"
          :disabled="isSaving"
        />
      </div>

      <!-- 설명 -->
      <div class="space-y-2">
        <label class="block text-sm text-muted-foreground" :for="descId"
          >설명</label
        >
        <textarea
          :id="descId"
          v-model="form!.description"
          class="min-h-[120px] resize-y w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
          placeholder="(북마크에 대한 설명을 남겨보세요.)"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import CloseIcon from "@/components/icons/CloseIcon.vue";
import SaveIcon from "@/components/icons/SaveIcon.vue";
import { useWorkspaceStore } from "@/stores/workspace";
import type { ID, ImageMode } from "@/types/common";
import { storeToRefs } from "pinia";
import { computed, onMounted, onUnmounted, ref } from "vue";
import EmojiPicker from "vue3-emoji-picker";
import "vue3-emoji-picker/css";
import TagInput from "./TagInput.vue";

const props = defineProps<{
  collectionId: ID | null;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (
    e: "submit",
    payload: {
      title?: string | null;
      url: string;
      description?: string | null;
      emoji?: string | null;
      imageMode?: ImageMode;
      collectionId: ID;
      tags?: string[];
    },
  ): void;
}>();

const { isMutating } = storeToRefs(useWorkspaceStore());

const isSaving = computed(() => isMutating.value.createBookmark);

defineExpose({ focusUrl });

const form = ref({
  title: "",
  url: "",
  description: "",
  emoji: null as string | null,
  imageMode: "AUTO" as ImageMode,
  tags: [] as string[],
});

const urlRef = ref<HTMLInputElement | null>(null);

const uid = Math.random().toString(36).slice(2);
const titleId = `add-bm-title-${uid}`;
const urlId = `add-bm-url-${uid}`;
const descId = `add-bm-desc-${uid}`;

const isUrlValid = computed(() => {
  const normalized = normalizeUrl(form.value.url);

  if (!normalized) return false;

  try {
    new URL(normalized);
    return true;
  } catch {
    return false;
  }
});
const canSave = computed(
  () => isUrlValid.value && props.collectionId !== null && !isSaving.value,
);

const normalize = (s?: string | null) => {
  const v = (s ?? "").trim();
  return v ? v : null;
};

const normalizeUrl = (url?: string | null) => {
  const v = (url ?? "").trim();

  if (!v) return "";

  const lower = v.toLowerCase();
  if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
    return `https://${v}`;
  }

  return v;
};

// 핸들러
function focusUrl() {
  urlRef.value?.focus();
}
function resetForm() {
  form.value = {
    title: "",
    url: "",
    description: "",
    emoji: null,
    imageMode: "AUTO",
    tags: [],
  };
  emojiPickerOpen.value = false;
}
function handleSubmit() {
  if (isSaving.value) return;
  if (!canSave.value || props.collectionId === null) return;

  const tags = form.value.tags?.length ? form.value.tags : undefined;

  emit("submit", {
    title: normalize(form.value.title),
    url: normalizeUrl(form.value.url),
    description: normalize(form.value.description),
    emoji: form.value.emoji,
    imageMode: form.value.imageMode,
    collectionId: props.collectionId,
    tags,
  });
  resetForm();
}
function toggleAutoCover() {
  form.value.imageMode = form.value.imageMode === "AUTO" ? "NONE" : "AUTO";
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
  emojiPickerOpen.value = !emojiPickerOpen.value;
}
function onEmojiSelected(emoji: any) {
  const picked = emoji?.i ?? null;
  if (!picked) return;

  form.value.emoji = picked;
  closeEmojiPicker();
}
function removeEmoji() {
  form.value.emoji = null;
  closeEmojiPicker();
}

function onDocPointerDown(e: PointerEvent) {
  if (!emojiPickerOpen.value) return;

  const target = e.target as Node | null;
  if (!target) return;

  if (emojiPopoverRef.value?.contains(target)) return; // picker 내부 클릭 시
  if (emojiTriggerEl.value?.contains(target)) return; // 버튼 클릭이면, 토글 로직이 처리함

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
onMounted(() => {
  document.addEventListener("pointerdown", onDocPointerDown, true);
  document.addEventListener("keydown", onDocKeyDown);
});
onUnmounted(() => {
  document.removeEventListener("pointerdown", onDocPointerDown, true);
  document.removeEventListener("keydown", onDocKeyDown);
});
</script>
