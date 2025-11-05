<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    :stroke-width="strokeWidth"
    stroke-linecap="round"
    stroke-linejoin="round"
    class="inline-block"
    :style="{ transform: `rotate(${deg}deg)` }"
    :aria-label="title"
    aria-hidden="true"
    v-bind="$attrs"
  >
    <path d="M6 14l6-6 6 6" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from "vue";

defineOptions({ name: "ChevronIcon" });

const props = withDefaults(
  defineProps<{
    direction?: "up" | "down" | "left" | "right";
    size?: number | string;
    title?: string;
    strokeWidth?: number | string;
  }>(),
  {
    direction: "right",
    size: 16,
    title: "Chevron icon",
    strokeWidth: 2,
  }
);

const deg = computed(() => {
  switch (props.direction) {
    case "right":
      return 90;
    case "down":
      return 180;
    case "left":
      return 270;
    default:
      return 0; // up
  }
});
</script>
