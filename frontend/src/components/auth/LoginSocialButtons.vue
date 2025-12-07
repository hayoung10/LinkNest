<template>
  <div class="grid gap-5">
    <!-- Google (outline) -->
    <button
      type="button"
      class="w-full h-14 rounded-xl px-6 flex items-center justify-center gap-3 border border-gray-200 bg-white text-gray-800 hover:bg-gray-50 active:translate-y-[0.5px] focus-visible:ring-4 focus-visible:ring-blue-400/25 transition"
      @click="$emit('google')"
    >
      <GoogleIcon :size="20" />
      <span class="font-medium text-[15px]">구글로 계속하기</span>
    </button>

    <!-- Kakao -->
    <button
      type="button"
      class="w-full h-14 rounded-xl px-6 flex items-center justify-center gap-3 bg-[#F2DD4C] text-gray-900 hover:bg-[#E9D53E] active:translate-y-[0.5px] focus-visible:ring-4 focus-visible:ring-yellow-300/30 transition"
      @click="$emit('kakao')"
    >
      <KakaoIcon :size="20" />
      <span class="font-medium text-[15px]">카카오로 계속하기</span>
    </button>

    <!-- Naver -->
    <button
      type="button"
      class="w-full h-14 rounded-xl px-6 flex items-center justify-center gap-3 bg-[#56B86A] text-white hover:bg-[#4EAA60] active:translate-y-[0.5px] focus-visible:ring-4 focus-visible:ring-green-300/30 transition"
      @click="$emit('naver')"
    >
      <NaverIcon :size="20" />
      <span class="font-medium text-[15px]">네이버로 계속하기</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import GoogleIcon from "@/components/icons/GoogleIcon.vue";
import KakaoIcon from "@/components/icons/KakaoIcon.vue";
import NaverIcon from "@/components/icons/NaverIcon.vue";

defineEmits(["google", "kakao", "naver"]);
</script>
