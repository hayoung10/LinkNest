<template>
  <div
    class="flex flex-wrap items-center gap-2 rounded-md border border-border/60 px-2 py-2 bg-background"
    :class="disabled && 'opacity-50 cursor-not-allowed'"
  >
    <span
      v-for="tag in modelValue"
      :key="tag"
      class="inline-flex items-center gap-1 rounded-full border border-border/60 bg-muted/40 px-2 py-1 text-xs"
    >
      {{ tag }}
      <button
        v-if="!disabled"
        type="button"
        class="flex items-center justify-center rounded-full hover:bg-accent transition-colors"
        aria-label="태그 제거"
        @click="remove(tag)"
      >
        <CloseIcon class="size-3" />
      </button>
    </span>

    <input
      v-if="!disabled && modelValue.length < max"
      v-model="draft"
      type="text"
      class="min-w-[80px] flex-1 bg-transparent px-1 py-1 text-sm outline-none placehoder:text-muted-foreground/60"
      :placeholder="placeholder"
      @keydown.enter.prevent="commitDraft"
      @keydown.comma.prevent="commitDraft"
      @blur="commitDraft"
      @paste="onPaste"
    />
  </div>
</template>

<script setup lang="ts">
import CloseIcon from "@/components/icons/CloseIcon.vue";
import { computed, ref } from "vue";

const props = withDefaults(
  defineProps<{
    modelValue: string[];
    disabled?: boolean;
    max?: number;
    placeholder?: string;
  }>(),
  {
    disabled: false,
    max: 3,
    placehoder: "",
  }
);

const emit = defineEmits<{ (e: "update:modelValue", v: string[]): void }>();

const draft = ref("");

const isFull = computed(() => props.modelValue.length >= props.max);

function normalize(raw: string) {
  const v = raw.trim().replace(/\s+/g, " ");
  return v || null;
}

function remove(tag: string) {
  if (props.disabled) return;

  const next = props.modelValue.filter((t) => t !== tag);
  emit("update:modelValue", next);
}

function commitDraft() {
  if (props.disabled) return;
  if (isFull.value) return;

  if (!draft.value.trim()) return;

  const parts = draft.value
    .split(/[,\n]/g)
    .map((t) => normalize(t))
    .filter((t): t is string => !!t);

  if (!parts.length) {
    draft.value = "";
    return;
  }

  const unique = new Set(props.modelValue);
  const toAdd: string[] = [];

  for (const p of parts) {
    if (unique.has(p)) continue;
    toAdd.push(p);
    unique.add(p);

    if (props.modelValue.length + toAdd.length >= props.max) break;
  }

  if (toAdd.length) {
    emit("update:modelValue", [...props.modelValue, ...toAdd]);
  }

  draft.value = "";
}

function onPaste(e: ClipboardEvent) {
  // 붙여넣기: "a, b, c" 형태면 분리해서 추가
  if (props.disabled) return;
  if (isFull.value) return;

  const text = e.clipboardData?.getData("text") ?? "";
  if (!text) return;

  e.preventDefault();

  draft.value = text;
  commitDraft();
}
</script>
