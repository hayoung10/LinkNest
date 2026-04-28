<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <path d="M18 13v6a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
    <polyline points="15 3 21 3 21 9" />
    <line x1="10" y1="14" x2="21" y2="3" />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "ExternalLinkIcon" });

withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
  }>(),
  {
    size: 16,
    title: "External link icon",
    klass: "",
    strokeWidth: 2,
  }
);
</script>
