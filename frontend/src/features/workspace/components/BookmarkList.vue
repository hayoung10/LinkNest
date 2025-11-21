<template>
  <main class="flex-1 overflow-auto bg-background">
    <!-- 선택 없음 -->
    <div
      v-if="!hasSelection"
      class="h-full min-h-[60vh] grid place-items-center text-muted/70 py-16"
    >
      <div class="text-center space-y-2">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="size-14 mx-auto opacity-30"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
        >
          <path
            d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
          />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10 9 9 9 8 9" />
        </svg>
        <p>컬렉션을 선택해주세요.</p>
      </div>
    </div>

    <!-- 컬렉션 선택됨 -->
    <div v-else class="max-w-4xl mx-auto p-8 animate-fadeIn">
      <!-- 헤더 -->
      <header class="flex items-center justify-between mb-2">
        <div class="min-w-0 flex items-center gap-2 pl-3">
          <FolderIcon size="28" class="text-muted-foreground opacity-80" />
          <h2 class="text-xl font-semibold text-foreground truncate">
            {{ collection?.name }}
          </h2>
        </div>

        <div class="flex text-center gap-2 pr-3">
          <button
            type="button"
            class="inline-flex items-center gap-1 px-3.5 py-1.5 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 transition-colors"
            aria-label="새 북마크 추가"
            @click="$emit('open-add')"
          >
            <PlusIcon :size="16" klass="shrink-0" />
            <span>추가</span>
          </button>
        </div>
      </header>

      <div class="divider" />

      <!-- 북마크 리스트 -->
      <template v-if="bookmarks.length">
        <ul class="mt-0" role="list" aria-label="북마크 목록">
          <template v-for="b in bookmarks" :key="b.id">
            <li
              class="px-2 py-3 rounded-md hover:bg-accent cursor-pointer select-none"
            >
              <button
                type="button"
                class="w-full text-left px-3"
                @click="onSelect(b)"
                :title="displayTitle(b)"
              >
                <div class="text-sm font-semibold flex items-center gap-2">
                  <span class="truncate">{{ b.title }}</span>

                  <!-- 링크 아이콘 -->
                  <a
                    :href="b.url"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="p-1 text-neutral-400 hover:text-neutral-700 dark:hover:text-neutral-200 transition-colors"
                    aria-label="링크 새 탭에서 열기"
                    @click.stop
                  >
                    <ExternalLinkIcon :size="16" />
                  </a>
                </div>
                <div
                  class="mt-1 text-xs text-neutral-500 dark:text-neutral-400 flex items-center gap-2"
                >
                  <span class="truncate">{{ domain(b.url) }}</span>
                  <span aria-hidden="true">·</span>
                  <time :datetime="b.createdAt || ''">
                    {{ formatDate(b.createdAt) }}
                  </time>
                </div>
              </button>
            </li>

            <div class="divider mx-6" />
          </template>
        </ul>

        <footer
          class="mt-2 text-xs text-neutral-500 dark:text-neutral-400 text-center select-none"
        >
          {{ bookmarks.length }} 북마크
        </footer>
      </template>

      <!-- 빈 컬렉션 -->
      <div v-else class="text-sm text-muted-foreground py-12">
        <div class="flex flex-col items-center gap-3">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="size-12 opacity-30"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <path d="M3 7h18" />
            <path d="M6 7V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2" />
            <path d="M19 7v12a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V7" />
            <path d="M12 11v6" />
            <path d="M9 14h6" />
          </svg>
          <p class="text-center">링크를 추가하세요.</p>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Bookmark, Collection } from "@/types/common";
import FolderIcon from "@/components/icons/FolderIcon.vue";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";

const props = defineProps<{
  collection: Collection | null;
  bookmarks: Bookmark[];
}>();

const emit = defineEmits<{
  (e: "open-add"): void;
  (e: "select-bookmark", bookmark: Bookmark): void;
}>();

const hasSelection = computed(() => !!props.collection);
const bookmarks = computed<Bookmark[]>(() => props.bookmarks);

// 유틸
function displayTitle(b: Bookmark): string {
  const t = (b.title ?? "").trim();
  return t || "(제목 없음)";
}
function domain(url: string) {
  try {
    return new URL(url).host.replace(/^www\./, "");
  } catch {
    return url;
  }
}
function formatDate(iso?: string): string {
  if (!iso) return "-";
  try {
    return new Intl.DateTimeFormat("ko-KR", {
      year: "numeric",
      month: "numeric",
      day: "numeric",
    }).format(new Date(iso));
  } catch {
    return "-";
  }
}
function onSelect(b: Bookmark) {
  emit("select-bookmark", b);
}
</script>

<style scoped>
.divider {
  height: 1px;
  margin: 6px 4px;
  background: color-mix(in oklab, currentColor 12%, transparent);
  border-radius: 1px;
}
</style>
