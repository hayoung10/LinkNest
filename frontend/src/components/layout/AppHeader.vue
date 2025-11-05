<template>
  <header
    class="fixed top-0 left-0 right-0 z-50 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md border-b border-border"
  >
    <div class="container mx-auto px-4 h-16 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <div
          class="size-8 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg flex items-center justify-center"
        >
          <LogoIcon :size="20" klass="text-white" />
        </div>
        <span class="font-semibold">LinkNest</span>
      </div>

      <nav class="hidden md:flex items-center gap-8">
        <a href="#features" class="hover:text-primary transition-colors"
          >기능</a
        >
        <a href="#about" class="hover:text-primary transition-colors">소개</a>
      </nav>

      <button
        class="h-10 px-4 inline-flex items-center justify-center rounded-lg bg-gray-900 text-white hover:bg-gray-800 dark:bg-white dark:text-gray-900 dark:hover:bg-gray-200"
        @click="handleClick"
      >
        {{ auth.isLoggedIn ? "로그아웃" : "로그인" }}
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import LogoIcon from "../icons/LogoIcon.vue";

const router = useRouter();
const auth = useAuthStore();

async function handleClick() {
  if (auth.isLoggedIn) {
    await auth.logout();
    router.replace({ name: "login" });
  }
  router.push({ name: "login" });
}
</script>
