<template>
  <Icon :size="size" :title="title" :klass="klass">
    <path
      d="M9 3h6"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
    />
    <path
      d="M10 3v5.5L5.5 16.5A2 2 0 0 0 7.2 19.5h9.6a2 2 0 0 0 1.7-3L14 8.5V3"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    <path
      d="M7.2 14h9.6"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
    />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "TestIcon" });

const props = withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
  }>(),
  {
    size: 20,
    title: "Test account icon",
    klass: "",
    strokeWidth: 2,
  },
);
</script>
