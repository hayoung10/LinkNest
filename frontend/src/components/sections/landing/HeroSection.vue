<template>
  <section class="pt-32 pb-20 px-4">
    <div class="container mx-auto max-w-5xl text-center">
      <div
        class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-800 mb-6"
      >
        <svg
          class="size-4 text-blue-600 dark:text-blue-400"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M12 3l1.5 3L17 7.5 13.5 9 12 12 10.5 9 7 7.5 10.5 6 12 3z" />
          <path
            d="M5 3l2 4 4 2-4 2-2 4-2-4-4-2 4-2 2-4z"
            opacity="0.2"
            fill="currentColor"
          />
        </svg>
        <span class="text-sm text-blue-600 dark:text-blue-400"
          >링크 관리의 새로운 방법</span
        >
      </div>

      <h1
        class="text-4xl md:text-6xl mb-6 bg-gradient-to-r from-gray-900 to-gray-600 dark:from-white dark:to-gray-400 bg-clip-text text-transparent"
      >
        모든 링크를 한곳에,<br />LinkNest에서 시작하세요
      </h1>

      <p
        class="text-lg md:text-xl text-muted-foreground mb-8 max-w-2xl mx-auto"
      >
        흩어진 링크들을 깔끔하게 정리하고, 어디서든 쉽게 접근하세요. LinkNest로
        당신의 디지털 공간을 정리해보세요.
      </p>

      <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <button
          class="h-11 px-6 inline-flex items-center justify-center rounded-lg bg-gray-900 text-white hover:bg-gray-800 dark:bg-white dark:text-gray-900 dark:hover:bg-gray-200 text-base"
          @click="handleClick"
        >
          {{ auth.isLoggedIn ? "워크스페이스로 이동" : "로그인" }}
        </button>
      </div>

      <div
        class="mt-16 rounded-xl border border-border bg-white dark:bg-gray-900 shadow-2xl overflow-hidden"
      >
        <div
          class="aspect-video bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-800 dark:to-gray-900 flex items-center justify-center"
        >
          <div class="text-muted-foreground">Product Preview</div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";

const router = useRouter();
const auth = useAuthStore();

function handleClick() {
  if (auth.isLoggedIn) {
    router.push({ name: "workspace" });
  } else {
    router.push({ name: "login" });
  }
}
</script>
