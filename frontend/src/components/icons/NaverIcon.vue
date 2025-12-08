<template>
  <Icon :size="size" :title="title" :klass="klass">
    <path
      d="M13.3333 10.8333L6.66667 0H0V20H6.66667V9.16667L13.3333 20H20V0H13.3333V10.8333Z"
      fill="white"
    />
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "NaverIcon" });

const props = withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
  }>(),
  {
    size: 20,
    title: "Naver icon",
    klass: "",
    strokeWidth: 2,
  }
);
</script>
