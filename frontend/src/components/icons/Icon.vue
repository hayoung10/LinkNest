<template>
  <svg
    :width="sizePx"
    :height="sizePx"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
    :class="klass"
    role="img"
    :aria-label="title"
    :aria-hidden="!title"
  >
    <slot />
  </svg>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string; // 추가 클래스
  }>(),
  {
    size: 16,
    klass: "",
  }
);

const sizePx = computed(() =>
  typeof props.size === "number" ? `${props.size}px` : props.size
);
</script>
