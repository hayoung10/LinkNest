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

      <!-- 북마크 카드 -->
      <template v-else>
        <div ref="listWrapRef" class="flex-1 min-h-0 overflow-y-auto pr-1">
          <div
            class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
            aria-label="북마크 카드 목록"
          >
            <article
              v-for="b in bookmarks"
              :key="b.id"
              :ref="(el) => setItemRef(b.id, el)"
              class="group relative flex flex-col rounded-xl border border-zinc-200 bg-white/90 hover:bg-zinc-50 shadow-sm hover:shadow-md transition overflow-hidden cursor-pointer"
              :class="[
                isActive(b)
                  ? 'ring-2 ring-blue-500 border-blue-500'
                  : isFocused(b)
                    ? 'ring-2 ring-amber-400 bg-amber-50'
                    : '',
              ]"
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
                  :disabled="isFavoriteMutating(b.id)"
                  :aria-label="b.isFavorite ? '즐겨찾기 해제' : '즐겨찾기 추가'"
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
                        }}</span></template
                      ></span
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
                <div class="min-w-0 flex-1 flex flex-col gap-0.5">
                  <span class="truncate"
                    ><template v-for="(c, i) in domainChunks(b)" :key="i">
                      <span :class="c.isHit ? highlightClass : ''">{{
                        c.text
                      }}</span>
                    </template></span
                  >
                  <time :datetime="b.updatedAt || ''">
                    {{ formatDate(b.updatedAt) }}
                  </time>
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
  { rootMargin: "300px", threshold: 0 },
);

onUnmounted(() => cleanup());

// ------------------------
// Search(검색)
// ------------------------
const isSearching = computed(() => workspace.bookmarksQ.trim().length > 0);
const searchQ = computed(() => workspace.bookmarksQ.trim().normalize("NFC"));

const emptySearchDescription = computed(
  () => `'${workspace.bookmarksQ}'에 대한 결과를 찾을 수 없습니다.`,
);

const highlightClass =
  "font-extrabold bg-yellow-200/40 dark:bg-yellow-400/20 rounded";

function scrollToTop() {
  const el = listWrapRef.value;
  if (!el) return;
  el.scrollTo({ top: 0, behavior: "auto" });
}

async function onSearched() {
  await nextTick();
  scrollToTop();
}

function escapeRegExp(s: string) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function splitHighlight(text: string, q: string) {
  const raw = (text ?? "").toString().normalize("NFC");
  const keyword = (q ?? "").trim().normalize("NFC");

  if (!keyword) {
    return [{ text: raw, isHit: false }];
  }

  const regex = new RegExp(`(${escapeRegExp(keyword)})`, "gi");

  const parts = raw.split(regex);

  return parts.map((part) => ({
    text: part,
    isHit: part.toLowerCase() === keyword.toLowerCase(),
  }));
}

function titleChunks(b: Bookmark) {
  return splitHighlight(displayTitle(b), searchQ.value);
}

function domainChunks(b: Bookmark) {
  return splitHighlight(domain(b.url), searchQ.value);
}

function tagChunks(tag: string) {
  return splitHighlight(tag, searchQ.value);
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

    el.scrollIntoView({ block: "nearest", behavior: "smooth" });

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
