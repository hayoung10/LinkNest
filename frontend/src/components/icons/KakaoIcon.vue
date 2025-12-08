<template>
  <Icon :size="size" :title="title" :klass="klass">
    <g transform="translate(2.5, 2) scale(0.9)">
      <path
        d="M10 0C4.477 0 0 3.517 0 7.86C0 10.515 1.663 12.845 4.184 14.24L3.155 18.082C3.054 18.458 3.485 18.754 3.804 18.531L8.286 15.402C8.851 15.474 9.422 15.512 10 15.512C15.523 15.512 20 11.995 20 7.652C20 3.309 15.523 0 10 0Z"
        fill="#000"
      />
    </g>
  </Icon>
</template>

<script setup lang="ts">
import Icon from "./Icon.vue";

defineOptions({ name: "KakaoIcon" });

const props = withDefaults(
  defineProps<{
    size?: number | string;
    title?: string;
    klass?: string;
    strokeWidth?: number | string;
  }>(),
  {
    size: 20,
    title: "Kakao icon",
    klass: "",
    strokeWidth: 2,
  }
);
</script>
