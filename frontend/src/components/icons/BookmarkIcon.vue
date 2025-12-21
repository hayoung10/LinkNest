<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <path d="M6 3h12a1 1 0 0 1 1 1v17l-7-4-7 4V4a1 1 0 0 1 1-1z" />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "BookmarkIcon" });

const props = withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
  }>(),
  {
    size: 16,
    title: "Bookmark icon",
    klass: "",
    strokeWidth: 2,
  }
);
</script>
