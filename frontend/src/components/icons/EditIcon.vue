<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <path d="M12 20h9" />
    <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "EditIcon" });

const props = withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
  }>(),
  {
    size: 16,
    title: "Edit icon",
    klass: "",
    strokeWidth: 2,
  }
);
</script>
