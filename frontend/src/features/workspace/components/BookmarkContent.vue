<template>
  <main class="flex-1 overflow-auto bg-background">
    <!-- 북마크 선택 안 됐을 때 -->
    <div v-if="!hasSelection" class="flex-1 flex text-muted-foreground">
      <div class="text-center space-y-2">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="size-12 mx-auto opacity-40"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path
            d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
          />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10 9 9 9 8 9" />
        </svg>
        <p>북마크를 선택해주세요</p>
      </div>
    </div>

    <!-- 북마크가 선택된 경우 -->
    <div v-else class="max-w-4xl mx-auto p-8">
      <!-- 헤더 -->
      <header class="flex mb-6">
        <div>
          <p v-if="collectionName" class="text-sm text-muted-foreground">
            {{ collectionName }}
          </p>
          <h1 class="text-2xl font-semibold">{{ bookmark!.title }}</h1>
        </div>
        <div class="flex gap-2">
          <button class="text-sm px-3 py-1.5 border rounded-md hover:bg-accent">
            수정
          </button>
          <button class="text-sm px-3 py-1.5 border rounded-md hover:bg-accent">
            삭제
          </button>
        </div>
      </header>

      <!-- 링크 카드 -->
      <div class="border rounded-lg p-4 bg-muted/30">
        <div class="flex justify-between gap-4">
          <a
            href="bookmark!.url"
            target="_blank"
            rel="noopener noreferrer"
            class="text-primary hover:underline truncate"
          >
            {{ bookmark!.url }}
          </a>
          <button
            class="text-sm px-3 py-1 border rounded-md hover:bg-accent"
            @click="openUrl"
          >
            방문
          </button>
        </div>
      </div>

      <!-- 설명 -->
      <div>
        <h3 class="font-medium mb-2">설명</h3>
        <p class="text-muted-foreground whitespace-pre-wrap">
          {{ bookmark!.description }}
        </p>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed } from "vue";

type Bookmark = {
  id: string;
  title: string;
  description: string;
  url: string;
  collectionId: string;
};

const props = defineProps<{
  bookmark: Bookmark | null;
  collectionName?: string;
}>();

const hasSelection = computed(() => !!props.bookmark);

function openUrl() {
  if (props.bookmark?.url) window.open(props.bookmark.url, "_blank");
}
</script>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.animate-fadeIn {
  animation: fadeIn 0.3s ease-in-out;
}
</style>
