<template>
  <main class="flex-1 flex flex-col bg-background overflow-hidden min-h-0">
    <div
      class="flex-1 flex flex-col w-full max-w-4xl mx-auto p-8 animate-fadeIn min-h-0"
    >
      <!-- 헤더 -->
      <BookmarkHeader
        :collection="null"
        :c-path="[]"
        :is-loading-path="false"
        :is-add-disabled="true"
        :show-path="false"
        :show-add="false"
        @searched="onSearched"
      >
        <template #title>
          <span class="shrink-0 text-amber-500">
            <StarIcon :size="20" filled />
          </span>
          <h2
            class="text-xl font-semibold text-foreground truncate leading-none"
          >
            즐겨찾기
          </h2>
        </template>
      </BookmarkHeader>

      <div class="divider" />

      <!-- 상태 분기 -->
      <BaseError
        v-if="hasError"
        title="북마크를 불러올 수 없습니다"
        :description="bookmarksError ?? undefined"
        :onRetry="onRetry"
      />

      <BaseLoading v-else-if="isInitialLoading" label="북마크를 불러오는 중…" />

      <BaseEmpty
        v-else-if="isEmpty && !isSearching"
        title="즐겨찾기한 북마크가 없습니다."
        description="별 아이콘을 눌러 즐겨찾기를 추가하세요."
      />

      <BaseEmpty
        v-else-if="isEmpty && isSearching"
        title="검색 결과가 없습니다."
        :description="emptySearchDescription"
      />

      <!-- 즐겨찾기 카드 -->
      <template v-else>
        <div ref="listWrapRef" class="flex-1 min-h-0 overflow-y-auto pr-1">
          <div
            class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 auto-rows-fr"
            aria-label="즐겨찾기 카드 목록"
          >
            <article
              v-for="b in bookmarks"
              :key="b.id"
              class="group relative flex flex-col rounded-xl border border-zinc-200 bg-white/90 hover:bg-zinc-50 shadow-sm hover:shadow-md transition overflow-hidden cursor-pointer"
              :class="isActive(b) ? 'ring-2 ring-blue-500 border-blue-500' : ''"
              @click="onSelect(b)"
              :title="displayTitle(b)"
            >
              <!-- 카드 커버 -->
              <div
                class="relative h-20 w-full overflow-hidden border-b"
                :class="isActive(b) ? 'border-blue-300/80' : 'border-border/60'"
              >
                <!-- favorite -->
                <button
                  type="button"
                  class="absolute top-2 left-2 z-20 inline-flex items-center justify-center size-8 rounded-md bg-white/85 backdrop-blur border border-white/60 shadow-sm hover:bg-white dark:bg-zinc-900/70 dark:border-zinc-700/60 dark:hover:bg-zinc-800 transition-colors"
                  :disabled="isMutatingFor(b.id)"
                  :aria-label="b.isFavorite ? '즐겨찾기 해제' : '즐겨찾기 추가'"
                  @click.stop.prevent="toggleFavorite(b)"
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

                <img
                  v-if="coverUrl(b)"
                  :src="coverUrl(b)!"
                  alt=""
                  class="h-full w-full object-cover"
                  draggable="false"
                />

                <div
                  v-else-if="isAutoPending(b)"
                  class="h-full w-full grid place-items-center"
                  title="커버 이미지 가져오는 중"
                >
                  <span
                    class="inline-block size-3 rounded-full bg-zinc-400/60 animate-pulse"
                  />
                </div>

                <div
                  v-else
                  class="h-full w-full grid place-items-center text-zinc-500/70 dark:text-zinc-400/60"
                  title="커버 없음"
                >
                  <BookmarkIcon :size="18" />
                </div>
              </div>

              <!-- 카드 상단: 제목 -->
              <div class="px-4 pt-3 pb-2">
                <h3
                  class="text-sm font-semibold leading-snug line-clamp-1 flex items-center gap-2"
                  :class="
                    hasTitle(b)
                      ? 'text-foreground'
                      : 'text-neutral-400 dark:text-neutral-500'
                  "
                >
                  <span
                    v-if="b.emoji"
                    class="text-base leading-none shrink-0"
                    aria-hidden="true"
                    >{{ b.emoji }}</span
                  >
                  <span class="min-w-0 truncate">
                    <template v-for="(c, i) in titleChunks(b)" :key="i">
                      <span :class="c.isHit ? highlightClass : ''">{{
                        c.text
                      }}</span>
                    </template>
                  </span>
                </h3>

                <!-- 태그 -->
                <div
                  class="mt-1 min-h-[18px] flex flex-wrap items-center gap-1.5"
                >
                  <template v-if="tagCount(b) > 0">
                    <span
                      v-for="t in visibleTags(b)"
                      :key="t"
                      class="inline-flex items-center rounded-full bg-muted/40 border border-border/60 px-2 py-0.5 text-[11px] text-blue-600/80 hover:text-blue-600 dark:text-blue-400/80 dark:hover:text-blue-400"
                      ><template v-for="(c, i) in tagChunks(t)" :key="i">
                        <span :class="c.isHit ? highlightClass : ''">{{
                          c.text
                        }}</span>
                      </template></span
                    >

                    <!-- 3개 초과 시 +n 표시 -->
                    <span
                      v-if="extraTagCount(b) > 0"
                      class="text-[11px] text-muted-foreground/70"
                      >+{{ extraTagCount(b) }}</span
                    >
                  </template>
                </div>

                <p
                  v-if="b.description"
                  class="mt-1 text-xs text-neutral-500 line-clamp-2"
                >
                  {{ b.description }}
                </p>
              </div>

              <!-- 카드 하단: 도메인 + 날짜 + 링크 아이콘 -->
              <div
                class="mt-auto px-4 pb-3 pt-2 flex items-center justify-between gap-2 text-xs text-neutral-500"
              >
                <div class="min-w-0 flex-1 flex flex-col gap-1">
                  <!-- 도메인 / 날짜 -->
                  <div class="min-w-0 flex items-center gap-2">
                    <span class="min-w-0 truncate"
                      ><template v-for="(c, i) in domainChunks(b)" :key="i">
                        <span :class="c.isHit ? highlightClass : ''">{{
                          c.text
                        }}</span>
                      </template></span
                    >

                    <span aria-hidden="true" class="shrink-0 opacity-60"
                      >·</span
                    >

                    <time class="truncate" :datetime="b.updatedAt || ''">
                      {{ formatDate(b.updatedAt) }}
                    </time>
                  </div>

                  <!-- 컬렉션 (이모지 + 이름) -->
                  <span
                    class="min-w-0 flex items-center gap-1 text-[11px] text-muted-foreground"
                    :title="collectionLabel(b)"
                  >
                    <span aria-hidden="true" class="shrink-0">{{
                      collectionEmoji(b)
                    }}</span>
                    <span
                      class="min-w-0 truncate"
                      :title="collectionLabel(b)"
                      >{{ collectionName(b) }}</span
                    >
                  </span>
                </div>

                <a
                  :href="b.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="shrink-0 p-1.5 rounded-full text-neutral-400 hover:text-neutral-700 dark:hover:text-neutral-200 transition-colors"
                  aria-label="링크 새 탭에서 열기"
                  @click.stop
                >
                  <ExternalLinkIcon :size="16" />
                </a>
              </div>
            </article>
          </div>

          <!-- 더 보기 -->
          <div class="py-2 flex justify-center">
            <button
              v-if="canLoadMore"
              type="button"
              class="px-3 py-1.5 text-xs rounded-md border border-border/60 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors disabled:opacity-50"
              :disabled="isLoadingBookmarks"
              @click="loadMore"
            >
              더 보기
            </button>

            <span
              v-else-if="isLoadingMore"
              class="text-xs text-muted-foreground"
              >불러오는 중…</span
            >

            <span v-else-if="isEndReached" class="text-xs text-muted-foreground"
              >마지막입니다</span
            >
          </div>

          <!-- 무한 스크롤 -->
          <div
            v-if="canLoadMore"
            ref="sentinelRef"
            class="h-8"
            aria-hidden="true"
          />
        </div>

        <footer
          class="mt-2 text-xs text-neutral-500 dark:text-neutral-400 text-center select-none"
        >
          {{ bookmarks.length }} 북마크
        </footer>
      </template>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import type { Bookmark, ID } from "@/types/common";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import BookmarkIcon from "@/components/icons/BookmarkIcon.vue";
import StarIcon from "@/components/icons/StarIcon.vue";
import { useWorkspaceStore } from "@/stores/workspace";
import { storeToRefs } from "pinia";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";
import BookmarkHeader from "./BookmarkHeader.vue";
import { useBookmarkSearchUi } from "@/composables/useBookmarkSearchUi";
import { useBookmarkPagingScroll } from "@/composables/useBookmarkPagingScroll";
import { useBookmarkItemHelpers } from "@/composables/useBookmarkItemHelpers";
import { useBookmarkFavorite } from "@/composables/useBookmarkFavorite";
import { useBookmarkViewState } from "@/composables/useBookmarkViewState";
import { useBookmarkHighlights } from "@/composables/useBookmarkHighlights";

const props = defineProps<{
  selectedBookmarkId?: ID | null;
}>();

const emit = defineEmits<{
  (e: "select-bookmark", id: ID): void;
}>();

const workspace = useWorkspaceStore();

const { bookmarks } = storeToRefs(workspace);

const {
  isLoadingBookmarks,
  bookmarksError,
  hasError,
  isSearching,
  isInitialLoading,
  isEmpty,
} = useBookmarkViewState();

const isReady = computed(
  () => !hasError.value && !isLoadingBookmarks.value && !isEmpty.value,
);

function onRetry() {
  workspace.reloadBookmarks();
}

const { isMutatingFor, toggleFavorite } = useBookmarkFavorite();

const {
  displayTitle,
  hasTitle,
  domain,
  formatDate,
  coverUrl,
  isAutoPending,
  tagCount,
  visibleTags,
  extraTagCount,

  collectionEmoji,
  collectionName,
  collectionLabel,
} = useBookmarkItemHelpers();

function isActive(b: Bookmark): boolean {
  return props.selectedBookmarkId === b.id;
}

function onSelect(b: Bookmark) {
  emit("select-bookmark", b.id);
}

onMounted(() => {
  if (!workspace.collectionNodes.length)
    workspace.fetchCollectionTree().catch(() => {});
});

// ------------------------
// 더 보기
// ------------------------
const listWrapRef = ref<HTMLElement | null>(null);
const sentinelRef = ref<HTMLElement | null>(null);

const {
  canLoadMore,
  isLoadingMore,
  isEndReached,
  loadMore,
  cleanup,
  reconnect,
} = useBookmarkPagingScroll(listWrapRef, sentinelRef, {
  enabled: true,
  rootMargin: "300px",
  threshold: 0,
});

// ------------------------
// Search(검색)
// ------------------------
function scrollToTop() {
  const el = listWrapRef.value;
  if (!el) return;
  el.scrollTo({ top: 0, behavior: "auto" });
}

const { searchQ, emptySearchDescription, onSearched } = useBookmarkSearchUi({
  scrollToTop,
  afterSearched: reconnect,
});

const { highlightClass, titleChunks, domainChunks, tagChunks } =
  useBookmarkHighlights({
    searchQ,
    getTitle: displayTitle,
    getDomain: (b) => domain(b.url),
  });

// ------------------------
// watchers
// ------------------------
watch(
  () => isReady.value,
  async (ready) => {
    if (!ready) {
      cleanup();
      return;
    }
    await reconnect();
  },
  { immediate: true },
);
</script>

<style scoped>
.divider {
  height: 1px;
  margin: 6px 4px;
  background: color-mix(in oklab, currentColor 12%, transparent);
  border-radius: 1px;
}
</style>
