<template>
  <span class="inline-flex items-center justify-center" :class="klass">
    <GoogleIcon v-if="provider === 'GOOGLE'" :size="size" />
    <KakaoIcon v-else-if="provider === 'KAKAO'" :size="size" />
    <TestIcon v-else-if="provider === 'TEST'" :size="size" />
    <!-- <NaverIcon v-else-if="provider === 'NAVER'" :size="size" /> -->
  </span>
</template>

<script setup lang="ts">
import GoogleIcon from "@/components/icons/GoogleIcon.vue";
import KakaoIcon from "@/components/icons/KakaoIcon.vue";
import TestIcon from "@/components/icons/TestIcon.vue";
// import NaverIcon from "@/components/icons/NaverIcon.vue";
import type { Provider } from "@/types/common";

const props = withDefaults(
  defineProps<{
    provider: Provider;
    size?: number | string;
    klass?: string;
  }>(),
  {
    size: 20,
    klass: "",
  },
);
</script>
