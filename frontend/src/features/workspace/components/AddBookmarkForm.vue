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
      <p class="mt-1 text-sm text-muted-foreground">
        저장하고 싶은 링크의 정보를 입력해주세요.
      </p>

      <!-- 제목 -->
      <div class="space-y-2">
        <input
          :id="titleId"
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
          ref="urlRef"
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
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="size-6"
          aria-hidden="true"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
        >
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
        <span>취소</span>
      </button>
      <button
        type="button"
        class="inline-flex items-center h-9 gap-1.5 px-3 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
        :disabled="!canSave"
        @click="handleSubmit"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="size-4"
          aria-hidden="true"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
        >
          <path
            d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"
          />
          <path d="M17 21v-8H7v8" />
          <path d="M7 3v5h8" />
        </svg>
        <span>저장</span>
      </button>
    </footer>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";

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

const form = ref({ title: "", url: "", description: "" });

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
const canSave = computed(() => isUrlValid.value && props.collectionId !== null);

const normalize = (s?: string | null) => {
  const v = (s ?? "").trim();
  return v ? v : null;
};

// 핸들러
function handleClose() {
  emit("close");
}
function resetForm() {
  form.value = { title: "", url: "", description: "" };
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

watch(
  () => props.open,
  async (open) => {
    if (open) {
      await nextTick();
      urlRef.value?.focus();
    } else {
      resetForm();
    }
  }
);
</script>
