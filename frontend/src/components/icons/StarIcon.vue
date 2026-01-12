<template>
  <Icon :size="size" :title="title" :klass="klass" :stroke-width="strokeWidth">
    <path
      d="M12 3.5
      L14.95 9.36
      L21.5 10.3
      L16.75 14.93
      L17.9 21.5
      L12 18.4
      L6.1 21.5
      L7.25 14.93
      L2.5 10.3
      L9.05 9.36
      Z"
      :fill="filled ? 'currentColor' : 'none'"
      stroke="currentColor"
      stroke-linejoin="round"
    />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "StarIcon" });

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
    title: "Favorite",
    klass: "",
    strokeWidth: 2,
    filled: false,
  }
);
</script>
