<template>
  <section class="h-full flex flex-col bg-card text-card-foreground">
    <!-- 헤더 -->
    <header class="sticky top-0 z-10 bg-card border-b border-border">
      <div class="relative flex items-center justify-between px-4 py-3">
        <h2 class="text-[15px] font-semibold">새 북마크 추가</h2>

        <button
          type="button"
          :disabled="isSaving"
          class="absolute right-2 top-2 p-2 rounded-md hover:bg-accent"
          aria-label="패널 닫기"
          @click="$emit('close')"
        >
          ✕
        </button>
      </div>
    </header>

    <!-- 본문 -->
    <div class="flex-1 overflow-auto px-5 py-4 space-y-6">
      <p class="text-sm text-neutral-500 dark:text-neutral-400 pb-4">
        저장하고 싶은 링크의 정보를 입력해주세요.
      </p>

      <!-- 제목 -->
      <div class="space-y-2">
        <!-- 이모지 -->
        <div class="relative flex items-end gap-2">
          <button
            ref="emojiTriggerEl"
            type="button"
            :class="[
              'relative flex items-center justify-center size-9 rounded-md transition-colors',
              form.emoji
                ? 'hover:bg-zinc-200 dark:hover:bg-zinc-800 cursor-pointer'
                : 'cursor-default',
            ]"
            @click="onEmojiStateClick"
            :aria-label="form.emoji ? '이모지 변경' : '이모지 상태'"
            title="이모지"
          >
            <span v-if="form.emoji" class="text-2xl leading-none">{{
              form.emoji
            }}</span>
            <span v-else class="text-lg leading-none opacity-20">+</span>
          </button>

          <button
            v-if="!form.emoji"
            type="button"
            class="mb-1.5 rounded-md px-2 py-1 text-xs leading-none text-muted-foreground/60 hover:bg-zinc-200 dark:hover:bg-zinc-800 hover:text-foreground transition-colors"
            @click="toggleEmojiPicker"
          >
            이모지 추가
          </button>
          <button
            v-else
            type="button"
            class="mb-1.5 rounded-md px-2 py-1 text-xs leading-none text-red-600/80 hover:text-red-700 dark:text-red-400/80 dark:hover:text-red-300 hover:bg-zinc-200 dark:hover:bg-zinc-800 transition-colors"
            @click="removeEmoji"
          >
            이모지 제거
          </button>

          <!-- EmojiPicker popover -->
          <div
            v-if="emojiPickerOpen"
            ref="emojiPopoverRef"
            class="absolute left-0 top-[34px] mt-2 z-50"
          >
            <EmojiPicker :native="true" @select="onEmojiSelected" />
          </div>
        </div>

        <!-- 제목 입력 -->
        <input
          :id="titleId"
          v-model="form.title"
          type="text"
          class="w-full border-0 border-b border-border/70 bg-transparent px-3 py-2.5 text-xl font-semibold placeholder:text-muted-foreground/60 focus:border-ring focus:outline-none focus:ring-1 focus:ring-ring/40"
          placeholder="(제목 없음)"
        />
      </div>

      <!-- 링크 -->
      <div class="space-y-2">
        <label class="block text-sm text-muted-foreground" :for="urlId"
          >링크 *</label
        >
        <input
          :id="urlId"
          ref="urlRef"
          v-model.trim="form!.url"
          type="url"
          class="w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
          placeholder="https://example.com"
          :aria-invalid="!isUrlValid"
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

    <footer
      class="sticky bottom-0 z-10 flex justify-end gap-2 border-t border-border bg-card/90 backdrop-blur supports-[backdrop-filter]:bg-card/70 px-4 py-3"
    >
      <button
        type="button"
        :disabled="isSaving"
        class="inline-flex items-center h-9 gap-1.5 px-3 rounded-md hover:bg-accent text-sm"
        @click="handleClose"
      >
        <CloseIcon class="size-6" />
        <span>취소</span>
      </button>
      <button
        type="button"
        class="inline-flex items-center h-9 gap-1.5 px-3 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
        :disabled="!canSave"
        @click="handleSubmit"
      >
        <SaveIcon class="size-6" />
        <span>저장</span>
      </button>
    </footer>
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

const props = defineProps<{
  open: boolean;
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
    }
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
});

const urlRef = ref<HTMLInputElement | null>(null);

const uid = Math.random().toString(36).slice(2);
const titleId = `add-bm-title-${uid}`;
const urlId = `add-bm-url-${uid}`;
const descId = `add-bm-desc-${uid}`;

const isUrlValid = computed(() => {
  const v = (form.value.url ?? "").trim();
  if (!v) return false;
  try {
    new URL(v);
    return true;
  } catch {
    return false;
  }
});
const canSave = computed(
  () => isUrlValid.value && props.collectionId !== null && !isSaving.value
);

const normalize = (s?: string | null) => {
  const v = (s ?? "").trim();
  return v ? v : null;
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
  };
  emojiPickerOpen.value = false;
}
function handleClose() {
  resetForm();
  emit("close");
}
function handleSubmit() {
  if (isSaving.value) return;
  if (!canSave.value || props.collectionId === null) return;
  emit("submit", {
    title: normalize(form.value.title),
    url: form.value.url.trim(),
    description: normalize(form.value.description),
    emoji: form.value.emoji,
    imageMode: form.value.imageMode,
    collectionId: props.collectionId,
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
function onEmojiStateClick() {
  if (!form.value.emoji) return;
  toggleEmojiPicker();
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
