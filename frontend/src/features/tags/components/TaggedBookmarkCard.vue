<template>
  <section class="flex-1 flex flex-col bg-background overflow-hidden min-h-0">
    <!-- 선택 없음 -->
    <BaseEmpty
      v-if="!hasSelection"
      title="태그를 선택해주세요."
      description="왼쪽 목록에서 태그를 선택하면 목록을 볼 수 있습니다."
    />

    <!-- 태그 선택됨 -->
    <template v-else>
      <!-- 헤더 -->
      <TagHeader
        variant="bookmarks"
        title="태그의 북마크"
        subtitle="선택한 태그가 포함된 북마크 목록입니다."
        :showBack="true"
        @back="$emit('back')"
      />

      <div class="px-6 py-6 flex-1 min-h-0 overflow-hidden">
        <div class="w-full max-w-4xl mx-auto flex flex-col min-h-0 h-full">
          <!-- 상태 분기 -->
          <BaseError
            v-if="hasError"
            title="북마크를 불러올 수 없습니다"
            :description="errorMessage"
            :onRetry="onRetry"
          />

          <BaseLoading
            v-else-if="isLoadingBookmarks && items.length === 0"
            label="북마크를 불러오는 중…"
          />

          <BaseEmpty
            v-else-if="isEmpty"
            title="북마크가 없습니다."
            description="이 태그가 연결된 북마크가 없습니다."
          />

          <!-- 북마크 카드 -->
          <template v-else>
            <!-- 선택 툴바 -->
            <TaggedBookmarkSelectionToolbar
              :selected-count="selectedCount"
              :all-checked="allChecked"
              :isTagMutating="isTagMutating"
              :candidates="replaceCandidates"
              :target-tag-id="bulkTargetTagId"
              :can-replace="canBulkReplace"
              @toggle-all="toggleAll"
              @detach="handleBulkDetach"
              @replace="handleBulkReplace"
              @update:target-tag-id="(v) => (bulkTargetTagId = v)"
            />

            <div ref="listWrapRef" class="flex-1 min-h-0 overflow-y-auto pr-1">
              <div
                class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 auto-rows-fr"
                aria-label="태그 북마크 카드 목록"
              >
                <article
                  v-for="b in items"
                  :key="b.id"
                  :ref="(el) => setItemRef(b.id, el)"
                  class="group relative flex flex-col rounded-xl border bg-white/90 transition overflow-hidden cursor-pointer"
                  :class="[
                    isChecked(b.id)
                      ? 'ring-2 ring-blue-400 border-blue-50'
                      : isActive(b)
                        ? 'ring-2 ring-blue-500 border-blue-500'
                        : isFocused(b)
                          ? 'ring-2 ring-amber-400 bg-amber-50 border-amber-300'
                          : 'border-zinc-200 hover:bg-zinc-50 hover:shadow-md',
                  ]"
                  @click="onSelect(b)"
                >
                  <!-- 카드 커버 -->
                  <div
                    class="relative h-20 w-full overflow-hidden border-b"
                    :class="
                      isChecked(b.id)
                        ? 'border-blue-200'
                        : isActive(b)
                          ? 'border-blue-300/80'
                          : isFocused(b)
                            ? 'border-amber-300'
                            : 'border-border/60'
                    "
                  >
                    <!-- 체크박스 -->
                    <div
                      class="absolute top-2 left-2 z-30"
                      :class="[
                        isChecked(b.id)
                          ? 'opacity-100 pointer-events-auto'
                          : 'opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto group-focus-within:opacity-100 group-focus-within:pointer-events-auto',
                        'transition-opacity',
                      ]"
                      @click.stop
                    >
                      <input
                        type="checkbox"
                        class="size-5 rounded border-zinc-300 text-blue-600 bg-white/90 backdrop-blur focus:ring-2 focus:ring-blue-500/40 dark:border-zinc-600 dark:bg-zinc-900/70"
                        :checked="isChecked(b.id)"
                        @click.stop="toggleChecked(b.id)"
                        :aria-label="isChecked(b.id) ? '선택 해제' : '선택'"
                      />
                    </div>

                    <!-- favorite (TODO: disabled) -->
                    <button
                      type="button"
                      class="absolute top-2 left-8 z-20 inline-flex items-center justify-center size-8 hover:bg-white/60 dark:hover:bg-zinc-800/60 transition-colors disabled:opacity-50"
                      :disabled="isMutatingFor(b.id)"
                      :aria-label="
                        b.isFavorite ? '즐겨찾기 해제' : '즐겨찾기 추가'
                      "
                      @click.stop.prevent="toggleFavorite(b)"
                    >
                      <StarIcon
                        :size="18"
                        :filled="b.isFavorite"
                        :klass="
                          b.isFavorite
                            ? 'text-amber-400'
                            : 'text-zinc-500/70 dark:text-zinc-200/70'
                        "
                      />
                    </button>

                    <!-- 메뉴 -->
                    <div
                      class="absolute top-2 right-2 z-20 opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto group-focus-within:opacity-100 group-focus-within:pointer-events-auto transition-opacity"
                    >
                      <TaggedBookmarkMoreMenu
                        v-if="props.tagId != null"
                        align="right"
                        :tag-id="props.tagId"
                        :bookmark-id="b.id"
                        :collection-id="b.collectionId"
                        @open-workspace="onOpenWorkspace"
                        @tags-changed="$emit('tags-changed')"
                      />
                    </div>

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
                        {{ displayTitle(b) }}
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
                          >{{ t }}</span
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
                        <span class="min-w-0 truncate">{{
                          domain(b.url)
                        }}</span>

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

                <span
                  v-else-if="isEndReached"
                  class="text-xs text-muted-foreground"
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
              {{ items.length }} 북마크
            </footer>
          </template>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import { storeToRefs } from "pinia";
import type { ID, TaggedBookmark } from "@/types/common";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import BookmarkIcon from "@/components/icons/BookmarkIcon.vue";
import StarIcon from "@/components/icons/StarIcon.vue";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";
import {
  TaggedBookmarkMoreMenu,
  TagHeader,
  TaggedBookmarkSelectionToolbar,
} from "@/features/tags";
import { useTaggedBookmarksStore } from "@/stores/taggedBookmarks";
import { useToastStore } from "@/stores/toast";
import { useTagsStore } from "@/stores/tags";
import { getErrorMessage } from "@/utils/errorMessage";
import { useInfiniteScroll } from "@/composables/useInfiniteScroll";
import { useBookmarkItemHelpers } from "@/composables/useBookmarkItemHelpers";
import { useFocusScroll } from "@/composables/useFocusScroll";
import { useBookmarkFavorite } from "@/composables/useBookmarkFavorite";

const props = defineProps<{
  tagId: ID | null;
  selectedBookmarkId?: ID | null;
  focusBookmarkId?: ID | null;
}>();

const emit = defineEmits<{
  (e: "select-bookmark", id: ID): void;
  (e: "back"): void;
  (e: "open-settings"): void;
  (e: "tags-changed"): void;
  (e: "open-workspace", payload: { bookmarkId: ID; collectionId: ID }): void;
  (e: "focus-done", id: ID): void;
}>();

const toast = useToastStore();
const taggedStore = useTaggedBookmarksStore();
const tagsStore = useTagsStore();

const { items, isLoading, error, loaded, isMutating } =
  storeToRefs(taggedStore);
const { items: tagItems } = storeToRefs(tagsStore);

const hasSelection = computed(() => props.tagId != null);

const isLoadingBookmarks = computed(() => isLoading.value);
const hasError = computed(() => !!error.value);
const errorMessage = computed(() => {
  if (!error.value) return undefined;
  return "네트워크 상태를 확인한 뒤 다시 시도해주세요.";
});

const isEmpty = computed(
  () =>
    hasSelection.value &&
    loaded.value &&
    !hasError.value &&
    items.value.length === 0,
);

const isTagMutating = computed(
  () => !!(isMutating.value.detach || isMutating.value.replace),
);

function onRetry() {
  taggedStore.safeReload();
}

const { isMutatingFor, toggleFavorite } = useBookmarkFavorite({
  disabled: isTagMutating,
  onToggled: ({ id, isFavorite }) => {
    taggedStore.patchTaggedBookmarkFavorite(id, isFavorite);
  },
});

// ------------------------
// Display
// ------------------------
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
} = useBookmarkItemHelpers();

function isActive(b: TaggedBookmark): boolean {
  return props.selectedBookmarkId === b.id;
}

function collectionLabel(b: TaggedBookmark): string {
  return `${collectionEmoji(b)} ${collectionName(b)}`.trim();
}

function collectionEmoji(b: TaggedBookmark): string {
  return b.collectionEmoji ?? "📁";
}

function collectionName(b: TaggedBookmark): string {
  const name = b.collectionName;
  return name?.trim() ? name.trim() : "로딩…";
}

function onSelect(b: TaggedBookmark) {
  if (selectedCount.value > 0) return;
  emit("select-bookmark", b.id);
}

function onOpenWorkspace(payload: { bookmarkId: ID; collectionId: ID }) {
  emit("open-workspace", payload);
}

// ------------------------
// Selection
// ------------------------
const selectedIds = ref<Set<ID>>(new Set());

const selectedCount = computed(() => selectedIds.value.size);
const selectedList = computed(() => Array.from(selectedIds.value));

function isChecked(id: ID) {
  return selectedIds.value.has(id);
}

function toggleChecked(id: ID) {
  const next = new Set(selectedIds.value);
  next.has(id) ? next.delete(id) : next.add(id);
  selectedIds.value = next;
}

function clearSelection() {
  selectedIds.value = new Set();
}

const allChecked = computed(() => {
  if (items.value.length === 0) return false;
  return items.value.every((b) => selectedIds.value.has(b.id));
});

function toggleAll() {
  if (allChecked.value) {
    clearSelection();
  } else {
    selectedIds.value = new Set(items.value.map((b) => b.id));
  }
}

// ------------------------
// detach / replace
// ------------------------
const bulkTargetTagId = ref<ID | "">("");

const replaceCandidates = computed(() => {
  const current = props.tagId;
  if (current == null) return [];
  return tagItems.value.filter((t) => String(t.id) !== String(current));
});

const canBulkReplace = computed(() => {
  if (!props.tagId) return false;
  if (!bulkTargetTagId.value) return false;
  return String(bulkTargetTagId.value) !== String(props.tagId);
});

async function handleBulkDetach() {
  if (!props.tagId) return;
  if (selectedCount.value === 0) return;

  try {
    await taggedStore.detachFromBookmarks(selectedList.value);
    emit("tags-changed");
    clearSelection();
  } catch (e) {
    toast.error(getErrorMessage(e, "태그 제거에 실패했습니다."));
  }
}

async function handleBulkReplace() {
  if (!props.tagId) return;
  if (selectedCount.value === 0) return;
  if (!canBulkReplace.value) return;

  try {
    const toTagId = Number(bulkTargetTagId.value);
    await taggedStore.replaceOnBookmarks(toTagId, selectedList.value);
    emit("tags-changed");
    clearSelection();
    bulkTargetTagId.value = "";
  } catch (e) {
    toast.error(getErrorMessage(e, "태그 교체에 실패했습니다."));
  }
}

// ------------------------
// Focus scroll
// ------------------------
const listWrapRef = ref<HTMLElement | null>(null);

function isFocused(b: TaggedBookmark): boolean {
  if (selectedCount.value > 0) return false;
  return props.focusBookmarkId === b.id;
}

const focusIdForScroll = computed<ID | null>(() => {
  if (selectedCount.value > 0) return null;
  return props.focusBookmarkId ?? null;
});

const isFocusBlocked = computed(
  () => isLoadingBookmarks.value || !loaded.value,
);

const { setItemRef } = useFocusScroll(
  focusIdForScroll,
  isFocusBlocked,
  (id) => emit("focus-done", id),
  { block: "center", doneDelayMs: 80 },
);

// ------------------------
// 더 보기
// ------------------------
const sentinelRef = ref<HTMLElement | null>(null);

const canLoadMore = computed(
  () => hasSelection.value && taggedStore.hasNext && !isLoadingBookmarks.value,
);
const isLoadingMore = computed(
  () =>
    hasSelection.value && isLoadingBookmarks.value && items.value.length > 0,
);
const isEndReached = computed(
  () => hasSelection.value && loaded.value && !taggedStore.hasNext,
);

async function loadMore() {
  if (!canLoadMore.value) return;

  const prevPage = taggedStore.page;

  const moved = taggedStore.nextPage();
  if (!moved) return;

  try {
    await taggedStore.load(true);
  } catch {
    taggedStore.setQuery({ page: prevPage });
  }
}

const { setup, cleanup } = useInfiniteScroll(
  listWrapRef,
  sentinelRef,
  loadMore,
  { rootMargin: "300px", threshold: 0 },
);

async function reconnect() {
  cleanup();
  await nextTick();
  setup();
}

// ------------------------
// Watchers
// ------------------------
watch(
  () => props.tagId,
  async (next) => {
    clearSelection();
    bulkTargetTagId.value = "";

    if (next == null) {
      cleanup();
      return;
    }

    await reconnect();
  },
  { immediate: true },
);

watch(
  () => items.value.map((b) => b.id).join(","),
  () => {
    const current = new Set(items.value.map((b) => b.id));
    const next = new Set<ID>();
    selectedIds.value.forEach((id) => {
      if (current.has(id)) next.add(id);
    });
    selectedIds.value = next;
  },
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
