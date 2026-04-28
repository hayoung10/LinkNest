<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <path d="M12 5v14" />
    <path d="M5 12h14" />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "PlusIcon" });

withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
  }>(),
  {
    size: 16,
    title: "Plus icon",
    klass: "",
    strokeWidth: 2,
  }
);
</script>
