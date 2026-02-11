<template>
  <main class="flex-1 flex flex-col bg-background overflow-hidden min-h-0">
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
    <div
      v-else
      class="flex-1 flex flex-col w-full max-w-4xl mx-auto p-8 animate-fadeIn min-h-0"
    >
      <!-- 헤더 -->
      <BookmarkHeader
        :collection="collection"
        :c-path="cPath"
        :is-loading-path="isLoadingPath"
        :is-add-disabled="isAddDisabled"
        @open-add="$emit('open-add')"
        @searched="onSearched"
        @clear-focus="$emit('clear-focus')"
      />

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
        title="북마크를 추가하세요."
        description="오른쪽 위 '추가' 버튼으로 링크를 추가하세요."
      />

      <BaseEmpty
        v-else-if="isEmpty && isSearching"
        title="검색 결과가 없습니다."
        :description="emptySearchDescription"
      />

      <!-- 북마크 리스트 -->
      <template v-else>
        <div ref="listWrapRef" class="flex-1 min-h-0 overflow-y-auto pr-1">
          <ul class="mt-0" role="list" aria-label="북마크 목록">
            <li
              v-for="b in bookmarks"
              :key="b.id"
              :ref="(el) => setItemRef(b.id, el)"
              class="px-2 py-3 rounded-md cursor-pointer select-none transition-colors"
              :class="
                isActive(b)
                  ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-100 ring-1 ring-blue-300'
                  : isFocused(b)
                    ? 'bg-amber-50 ring-1 ring-amber-300'
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
                        class="inline-flex items-center rounded-full bg-muted/40 border border-border/60 px-2 py-0.5 text-[11px] text-blue-600/80 hover:text-blue-600 dark:text-blue-400/80 dark:hover:text-blue-400"
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
          </ul>

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
import {
  ComponentPublicInstance,
  computed,
  nextTick,
  onUnmounted,
  ref,
  watch,
} from "vue";
import type { Bookmark, CollectionNode, ID } from "@/types/common";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import BookmarkIcon from "@/components/icons/BookmarkIcon.vue";
import StarIcon from "@/components/icons/StarIcon.vue";
import { useWorkspaceStore } from "@/stores/workspace";
import { storeToRefs } from "pinia";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";
import * as CollectionApi from "@/api/collections";
import { CollectionPathRes } from "@/api/types";
import { useToastStore } from "@/stores/toast";
import { useInfiniteScroll } from "@/composables/useInfiniteScroll";
import BookmarkHeader from "./BookmarkHeader.vue";

const props = defineProps<{
  collection: CollectionNode | null;
  selectedBookmarkId?: ID | null;
  focusBookmarkId?: ID | null;
}>();

const emit = defineEmits<{
  (e: "open-add"): void;
  (e: "select-bookmark", id: ID): void;
  (e: "focus-done", id: ID): void;
  (e: "clear-focus"): void;
}>();

const toast = useToastStore();
const workspace = useWorkspaceStore();
const {
  selectedCollectionId,
  collectionNodes,
  bookmarks,
  isLoading,
  error,
  isMutating,
} = storeToRefs(workspace);

const hasSelection = computed(() => selectedCollectionId.value != null);

const isLoadingBookmarks = computed(() => isLoading.value.bookmarks);
const bookmarksError = computed(() => error.value.bookmarks);
const hasError = computed(() => !!bookmarksError.value);

const isInitialLoading = computed(
  () =>
    isLoadingBookmarks.value &&
    bookmarks.value.length === 0 &&
    workspace.bookmarksQ.trim().length === 0,
);

const isEmpty = computed(
  () =>
    hasSelection.value &&
    !isLoadingBookmarks.value &&
    !hasError.value &&
    bookmarks.value.length === 0,
);

const isAddDisabled = computed(
  () =>
    !hasSelection.value ||
    isLoadingBookmarks.value ||
    hasError.value ||
    isMutating.value.createBookmark,
);

const path = ref<CollectionPathRes[]>([]);
const isLoadingPath = ref(false);
const cPath = computed(() => path.value.slice(0, -1));

function onRetry() {
  const cid = selectedCollectionId.value;
  if (cid != null) workspace.reloadBookmarks(cid);
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

async function refreshPath(cid: ID | null) {
  path.value = [];
  if (cid == null) return;

  isLoadingPath.value = true;
  try {
    path.value = await CollectionApi.getPath(cid);
  } finally {
    isLoadingPath.value = false;
  }
}

// ------------------------
// Focus scroll
// ------------------------
const itemRefs = new Map<ID, HTMLElement>();
const lastFocusedId = ref<ID | null>(null);

function setItemRef(id: ID, el: Element | ComponentPublicInstance | null) {
  const dom = el as unknown as HTMLElement | null;
  if (!dom) {
    itemRefs.delete(id);
    return;
  }
  itemRefs.set(id, dom);
}

function isFocused(b: Bookmark): boolean {
  if (props.selectedBookmarkId != null) return false;
  return props.focusBookmarkId === b.id;
}

// ------------------------
// 더 보기
// ------------------------
const listWrapRef = ref<HTMLElement | null>(null);
const sentinelRef = ref<HTMLElement | null>(null);

const canLoadMore = computed(
  () =>
    hasSelection.value &&
    workspace.bookmarksHasNext &&
    !isLoadingBookmarks.value,
);
const isLoadingMore = computed(
  () =>
    hasSelection.value &&
    isLoadingBookmarks.value &&
    bookmarks.value.length > 0,
);
const isEndReached = computed(
  () =>
    hasSelection.value &&
    workspace.bookmarksLoaded &&
    !workspace.bookmarksHasNext,
);

async function loadMore() {
  if (!canLoadMore.value) return;

  const prevPage = workspace.bookmarksPage;

  const moved = workspace.nextBookmarksPage();
  if (!moved) return;

  try {
    await workspace.fetchBookmarks(selectedCollectionId.value ?? undefined, {
      append: true,
    });
  } catch {
    workspace.setBookmarksQuery({
      bookmarksPage: prevPage,
      bookmarksLoaded: true,
    });
  }
}

const { setup, cleanup } = useInfiniteScroll(
  listWrapRef,
  sentinelRef,
  loadMore,
  { rootMargin: "200px", threshold: 0 },
);

onUnmounted(() => cleanup());

// ------------------------
// Search(검색)
// ------------------------
const isSearching = computed(() => workspace.bookmarksQ.trim().length > 0);

const emptySearchDescription = computed(
  () => `'${workspace.bookmarksQ}'에 대한 결과를 찾을 수 없습니다.`,
);

function scrollToTop() {
  const el = listWrapRef.value;
  if (!el) return;
  el.scrollTo({ top: 0, behavior: "auto" });
}

async function onSearched() {
  await nextTick();
  scrollToTop();
}

// ------------------------
// watchers
// ------------------------
watch(
  [() => selectedCollectionId.value, () => collectionNodes.value],
  ([cid]) => refreshPath(cid),
  { immediate: true },
);

watch(
  () => [props.focusBookmarkId, isLoadingBookmarks.value] as const,
  async ([id, loading]) => {
    if (id == null) return;
    if (loading) return;

    if (lastFocusedId.value === id) return;
    lastFocusedId.value = id;

    await nextTick();

    const el = itemRefs.get(id);
    if (!el) {
      emit("focus-done", id);
      return;
    }

    el.scrollIntoView({ block: "center", behavior: "smooth" });

    await new Promise((resolve) => window.setTimeout(resolve, 250));

    emit("focus-done", id);
  },
  { immediate: true },
);

watch(
  () => selectedCollectionId.value,
  async (cid) => {
    cleanup();
    if (cid == null) return;
    await nextTick();
    setup();
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
