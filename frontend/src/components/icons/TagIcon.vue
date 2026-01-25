<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <path
      d="M20.59 13.41 11 3.83 A2 2 0 0 0 9.59 3 H4 A2 2 0 0 0 2 5 V10.59 A2 2 0 0 0 2.83 12 L12.41 21.59 A2 2 0 0 0 15.24 21.59 L20.59 16.24 A2 2 0 0 0 20.59 13.41 Z"
      fill="none"
      stroke="currentColor"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    <circle
      cx="7.5"
      cy="7.5"
      r="1.5"
      fill="none"
      stroke="currentColor"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "TagIcon" });

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
    title: "Tag",
    klass: "",
    strokeWidth: 2,
    filled: false,
  },
);
</script>
