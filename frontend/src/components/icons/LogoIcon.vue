<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <path d="M10 13a5 5 0 0 1 7 0l2 2a5 5 0 0 1-7 7l-2-2" />
    <path d="M14 11a5 5 0 0 1-7 0l-2-2a5 5 0 1 1 7-7l2 2" />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
  }>(),
  {
    size: 16,
    title: "Logo icon",
    klass: "",
    strokeWidth: 2,
  }
);
</script>
