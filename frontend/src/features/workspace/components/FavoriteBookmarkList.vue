<template>
  <main class="flex-1 flex flex-col bg-background overflow-hidden min-h-0">
    <div
      class="flex-1 flex flex-col w-full max-w-4xl mx-auto p-8 animate-fadeIn min-h-0"
    >
      <!-- 헤더 -->
      <header class="flex items-center justify-between mb-2">
        <div class="min-w-0 pl-3">
          <!-- 여백 -->
          <div
            class="mb-1 ml-0.5 min-h-[16px] text-xs text-zinc-500 flex items-center gap-1 min-w-0"
            aria-hidden="true"
          ></div>

          <!-- 타이틀 -->
          <div class="min-w-0 flex items-center gap-2">
            <span class="shrink-0 text-amber-500">
              <StarIcon :size="20" filled />
            </span>
            <h2
              class="text-xl font-semibold text-foreground truncate leading-none"
            >
              즐겨찾기
            </h2>
          </div>
        </div>

        <!-- 우측 액션: (현재 없음) -->
        <div class="flex text-center gap-2 pr-3" aria-hidden="true">
          <!-- 헤더 높이 맞춤 -->
          <span
            class="inline-flex items-center px-3.5 py-1.5 opacity-0 select-none"
          >
            <span class="inline-block size-9" />
            <span>추가</span>
          </span>
        </div>
      </header>

      <div class="divider" />

      <!-- 상태 분기 -->
      <BaseError
        v-if="hasError"
        title="북마크를 불러올 수 없습니다"
        :description="bookmarksError ?? undefined"
        :onRetry="onRetry"
      />

      <BaseLoading
        v-else-if="isLoadingBookmarks"
        label="북마크를 불러오는 중…"
      />

      <BaseEmpty
        v-else-if="isEmpty"
        title="즐겨찾기한 북마크가 없습니다."
        description="별 아이콘을 눌러 즐겨찾기를 추가하세요."
      />

      <!-- 즐겨찾기 리스트 -->
      <template v-else>
        <div class="flex-1 min-h-0 overflow-y-auto pr-1">
          <ul class="mt-0" role="list" aria-label="즐겨찾기 리스트 목록">
            <template v-for="b in favoriteBookmarks" :key="b.id">
              <li
                class="px-2 py-3 rounded-md cursor-pointer select-none transition-colors"
                :class="
                  isActive(b)
                    ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-100 ring-1 ring-blue-300'
                    : 'hover:bg-zinc-100 dark:hover:bg-zinc-800'
                "
              >
                <button
                  type="button"
                  class="w-full text-left"
                  @click="onSelect(b)"
                  :title="displayTitle(b)"
                >
                  <div class="flex items-stretch gap-3 px-3">
                    <!-- favorite -->
                    <div class="shrink-0 pt-2">
                      <button
                        type="button"
                        class="shrink-0 inline-flex items-center justify-center size-8 rounded-md bg-white border border-zinc-300 shadow-sm hover:bg-zinc-50 dark:bg-zinc-900 dark:border-zinc-600 dark:hover:bg-zinc-800 transition-colors"
                        :disabled="isFavoriteMutating(b.id)"
                        :aria-label="
                          b.isFavorite ? '즐겨찾기 해제' : '즐겨찾기 추가'
                        "
                        @click.stop.prevent="onToggleFavorite(b)"
                      >
                        <StarIcon
                          :size="18"
                          :filled="b.isFavorite"
                          :klass="
                            b.isFavorite
                              ? 'text-amber-400'
                              : 'text-zinc-500/80 dark:text-zinc-200/80'
                          "
                        />
                      </button>
                    </div>

                    <!-- 왼쪽: cover -->
                    <span
                      class="shrink-0 h-14 w-14 rounded-xl overflow-hidden border bg-zinc-200/70 dark:bg-zinc-800/70"
                      :class="[
                        isActive(b)
                          ? 'border-blue-400/70 ring-2 ring-blue-300/60 dark:ring-blue-800/50'
                          : 'border-border/60',
                      ]"
                      aria-hidden="true"
                    >
                      <img
                        v-if="coverUrl(b)"
                        :src="coverUrl(b) ?? ''"
                        alt=""
                        class="h-full w-full object-cover"
                        draggable="false"
                      />

                      <span
                        v-else-if="isAutoPending(b)"
                        class="h-full w-full grid place-items-center text-zinc-500/70 dark:text-zinc-400/60"
                        title="커버 이미지 가져오는 중"
                      >
                        <span
                          class="inline-block size-2.5 rounded-full bg-zinc-400/60 animate-pulse"
                      /></span>

                      <span
                        v-else
                        class="h-full w-full grid place-items-center text-zinc-500/70 dark:text-zinc-400/60"
                        title="커버 없음"
                      >
                        <BookmarkIcon :size="16" />
                      </span>
                    </span>

                    <!-- 오른쪽: content -->
                    <div class="min-w-0 flex-1 py-0.5">
                      <div class="flex items-center gap-2">
                        <!-- emoji -->
                        <span
                          class="shrink-0 inline-flex items-center justify-center size-5 rounded"
                          :class="[
                            b.emoji
                              ? 'text-base'
                              : 'text-zinc-400/70 dark:text-zinc-500/70',
                            isActive(b) && !b.emoji
                              ? 'text-zinc-600 dark:text-zinc-300'
                              : '',
                          ]"
                          aria-hidden="true"
                        >
                          <template v-if="b.emoji">{{ b.emoji }}</template>
                          <template v-else>•</template></span
                        >

                        <!-- 제목 -->
                        <span
                          class="min-w-0 flex-1 truncate text-sm font-semibold"
                          :class="
                            hasTitle(b)
                              ? 'text-foreground'
                              : 'text-neutral-400 dark:text-neutral-500'
                          "
                          >{{ displayTitle(b) }}</span
                        >

                        <!-- 링크 아이콘 -->
                        <a
                          :href="b.url"
                          target="_blank"
                          rel="noopener noreferrer"
                          class="shrink-0 p-1 text-neutral-400 hover:text-neutral-700 dark:hover:text-neutral-200 transition-colors"
                          aria-label="링크 새 탭에서 열기"
                          @click.stop
                        >
                          <ExternalLinkIcon :size="16" />
                        </a>
                      </div>

                      <!-- 태그 -->
                      <div
                        v-if="tagCount(b) > 0"
                        class="mt-1 flex flex-wrap items-center gap-1.5"
                      >
                        <span
                          v-for="t in visibleTags(b)"
                          :key="t"
                          class="inline-flex items-center rounded-full bg-muted/40 border border-border/60 px-2 py-0.5 text-[11px] text-muted-foreground"
                          >{{ t }}</span
                        >

                        <!-- 3개 초과 시 +n 표시 -->
                        <span
                          v-if="extraTagCount(b) > 0"
                          class="text-[11px] text-muted-foreground/70"
                          >+{{ extraTagCount(b) }}</span
                        >
                      </div>

                      <div
                        class="mt-1 text-xs text-neutral-500 dark:text-neutral-400 flex items-center gap-2"
                      >
                        <span class="truncate">{{ domain(b.url) }}</span>
                        <span aria-hidden="true">·</span>
                        <time :datetime="b.updatedAt || ''">
                          {{ formatDate(b.updatedAt) }}
                        </time>
                      </div>
                    </div>
                  </div>
                </button>
              </li>

              <li aria-hidden="true">
                <div class="divider mx-6" />
              </li>
            </template>
          </ul>
        </div>

        <footer
          class="mt-2 text-xs text-neutral-500 dark:text-neutral-400 text-center select-none"
        >
          {{ favoriteBookmarks.length }} 북마크
        </footer>
      </template>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Bookmark, ID } from "@/types/common";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import BookmarkIcon from "@/components/icons/BookmarkIcon.vue";
import StarIcon from "@/components/icons/StarIcon.vue";
import { useWorkspaceStore } from "@/stores/workspace";
import { storeToRefs } from "pinia";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";
import { useToastStore } from "@/stores/toast";

const props = defineProps<{
  selectedBookmarkId?: ID | null;
}>();

const emit = defineEmits<{
  (e: "select-bookmark", id: ID): void;
}>();

const toast = useToastStore();
const workspace = useWorkspaceStore();

const { bookmarks, isLoading, error, isMutating } = storeToRefs(workspace);

const isLoadingBookmarks = computed(() => isLoading.value.bookmarks);
const bookmarksError = computed(() => error.value.bookmarks);
const hasError = computed(() => !!bookmarksError.value);

const favoriteBookmarks = computed(() => bookmarks.value);

const isEmpty = computed(
  () =>
    !isLoadingBookmarks.value &&
    !hasError.value &&
    favoriteBookmarks.value.length === 0
);

function onRetry() {
  workspace.fetchBookmarks();
}

function isFavoriteMutating(id: ID) {
  return isMutating.value.toggleBookmarkFavorite;
}

async function onToggleFavorite(b: Bookmark) {
  if (isFavoriteMutating(b.id)) return;

  try {
    await workspace.toggleBookmarkFavorite(b);
  } catch (e) {
    toast.error("즐겨찾기 변경에 실패했습니다.");
  }
}

function displayTitle(b: Bookmark): string {
  const t = (b.title ?? "").trim();
  return t || "(제목 없음)";
}

function hasTitle(b: Bookmark): boolean {
  return !!b.title?.trim();
}

function isActive(b: Bookmark): boolean {
  return props.selectedBookmarkId === b.id;
}

function tagCount(b: Bookmark) {
  return b.tags?.length ?? 0;
}

function extraTagCount(b: Bookmark, visible = 3) {
  return Math.max(tagCount(b) - visible, 0);
}

function visibleTags(b: Bookmark, visible = 3) {
  return b.tags?.slice(0, visible) ?? [];
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

function coverUrl(b: Bookmark): string | null {
  if (b.imageMode === "CUSTOM") return b.customImageUrl ?? null;
  if (b.imageMode === "AUTO") return b.autoImageUrl ?? null;
  return null;
}

function isAutoPending(b: Bookmark): boolean {
  return b.imageMode === "AUTO" && !b.autoImageUrl;
}

function onSelect(b: Bookmark) {
  emit("select-bookmark", b.id);
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
