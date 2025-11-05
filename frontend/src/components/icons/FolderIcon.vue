<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <path d="M3 7h5l2 2h11v9" />
    <path d="M3 7v10a2 2 0 0 0 2 2h16" />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "FolderIcon" });

withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
  }>(),
  {
    size: 16,
    title: "Folder icon",
    klass: "",
    strokeWidth: 2,
  }
);
</script>
