<template>
  <section class="h-full flex flex-col bg-card text-card-foreground">
    <!-- 헤더 -->
    <header class="sticky top-0 z-10 bg-card border-b border-border">
      <div class="relative flex items-center justify-between px-4 py-3">
        <h2 class="text-[15px] font-semibold">새 북마크 추가</h2>

        <button
          type="button"
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
        <input
          :id="titleId"
          ref="titleRef"
          v-model="form!.title"
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
          v-model.trim="form!.url"
          type="url"
          class="w-full rounded-md px-3 py-2 text-sm bg-muted/40 border border-border/60 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60 placeholder:text-muted-foreground/70"
          placeholder="https://example.com"
          :aria-invalid="!isUrlValid"
        />
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
import { computed, ref } from "vue";

const props = defineProps<{
  open: boolean;
  collectionId: number | null;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (
    e: "submit",
    payload: {
      title?: string | null;
      url: string;
      description?: string | null;
      collectionId: number;
    }
  ): void;
}>();

defineExpose({ focusTitle });

const form = ref({ title: "", url: "", description: "" });

const titleRef = ref<HTMLInputElement | null>(null);
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
const canSave = computed(() => isUrlValid.value && props.collectionId !== null);

const normalize = (s?: string | null) => {
  const v = (s ?? "").trim();
  return v ? v : null;
};

// 핸들러
function focusTitle() {
  titleRef.value?.focus();
}
function resetForm() {
  form.value = { title: "", url: "", description: "" };
}
function handleClose() {
  resetForm();
  emit("close");
}
function handleSubmit() {
  if (!canSave.value || props.collectionId === null) return;
  emit("submit", {
    title: normalize(form.value.title),
    url: form.value.url.trim(),
    description: normalize(form.value.description),
    collectionId: props.collectionId,
  });
  resetForm();
}
</script>
