<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <circle
      cx="11"
      cy="11"
      r="8"
      fill="none"
      stroke="currentColor"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    <line
      x1="21"
      y1="21"
      x2="16.65"
      y2="16.65"
      stroke="currentColor"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "SearchIcon" });

const props = withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
    filled?: boolean;
  }>(),
  {
    size: 16,
    title: "Search",
    klass: "",
    strokeWidth: 2,
    filled: false,
  },
);
</script>
