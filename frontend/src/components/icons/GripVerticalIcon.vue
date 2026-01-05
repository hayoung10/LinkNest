<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <circle cx="9" cy="5" r="1" fill="currentColor" stroke="none" />
    <circle cx="9" cy="12" r="1" fill="currentColor" stroke="none" />
    <circle cx="9" cy="19" r="1" fill="currentColor" stroke="none" />

    <circle cx="15" cy="5" r="1" fill="currentColor" stroke="none" />
    <circle cx="15" cy="12" r="1" fill="currentColor" stroke="none" />
    <circle cx="15" cy="19" r="1" fill="currentColor" stroke="none" />
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
    title: "Drag handle",
    klass: "",
    strokeWidth: 2,
  }
);
</script>
