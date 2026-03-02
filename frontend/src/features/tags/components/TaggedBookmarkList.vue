<template>
  <section class="h-full min-h-0 bg-background overflow-hidden flex flex-col">
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

          <!-- 북마크 리스트 -->
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
              @detach="openBulkDetach"
              @replace="openBulkReplace"
              @update:target-tag-id="(v) => (bulkTargetTagId = v)"
            />

            <div ref="listWrapRef" class="flex-1 min-h-0 overflow-y-auto pr-1">
              <ul class="mt-0" role="list" aria-label="태그 북마크 리스트 목록">
                <template v-for="b in items" :key="b.id">
                  <li
                    :ref="(el) => setItemRef(b.id, el)"
                    class="group px-2 py-3 rounded-md cursor-pointer select-none transition-colors"
                    :class="[
                      isChecked(b.id)
                        ? 'bg-blue-50 ring-1 ring-blue-300'
                        : isFocused(b)
                          ? 'bg-amber-50 ring-1 ring-amber-300'
                          : 'hover:bg-zinc-100 dark:hover:bg-zinc-800',
                    ]"
                    @click="onSelect(b)"
                  >
                    <div
                      type="button"
                      class="w-full text-left"
                      @click="toggleChecked(b.id)"
                      @keydown.enter.prevent="toggleChecked(b.id)"
                      @keydown.space.prevent="toggleChecked(b.id)"
                      :title="displayTitle(b)"
                    >
                      <div class="flex items-stretch gap-3 px-3">
                        <!-- 체크박스 -->
                        <div class="shrink-0 pt-2">
                          <div
                            class="size-5"
                            :class="[
                              isChecked(b.id)
                                ? 'opacity-100 pointer-events-auto'
                                : 'opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto group-focus-within:opacity-100 group-focus-within:pointer-events-auto',
                              'transition-opacity',
                            ]"
                          >
                            <input
                              type="checkbox"
                              class="size-5 rounded border-zinc-300 text-blue-600 focus:ring-2 focus:ring-blue-500/40 dark:border-zinc-600 dark:bg-zinc-900"
                              :checked="isChecked(b.id)"
                              @click.stop="toggleChecked(b.id)"
                              :aria-label="
                                isChecked(b.id) ? '선택 해제' : '선택'
                              "
                            />
                          </div>
                        </div>

                        <!-- favorite -->
                        <div class="shrink-0 pt-2">
                          <button
                            type="button"
                            class="shrink-0 inline-flex items-center justify-center size-8 rounded-md hover:bg-zinc-100/70 dark:hover:bg-zinc-800/60 transition-colors disabled:opacity-50"
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
                        </div>

                        <!-- 왼쪽: cover -->
                        <span
                          class="shrink-0 h-14 w-14 rounded-xl overflow-hidden border bg-zinc-200/70 dark:bg-zinc-800/70"
                          :class="[
                            isActive(b)
                              ? 'border-blue-400/70'
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

                            <div
                              class="shrink-0 flex items-center justify-end gap-1"
                            >
                              <!-- 링크 아이콘 -->
                              <a
                                :href="b.url"
                                target="_blank"
                                rel="noopener noreferrer"
                                class="inline-flex items-center justify-center size-8 rounded-md text-neutral-400 hover:text-neutral-700 dark:hover:text-neutral-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
                                aria-label="링크 새 탭에서 열기"
                                @click.stop
                              >
                                <ExternalLinkIcon :size="16" />
                              </a>

                              <!-- 메뉴 -->
                              <div
                                class="opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto group-focus-within:opacity-100 group-focus-within:pointer-events-auto transition-opacity"
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
                            </div>
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
                            <!-- 컬렉션 (이모지 + 이름) -->
                            <span
                              class="shrink-0 inline-flex items-center gap-1 text-[11px] text-muted-foreground"
                              :title="collectionLabel(b)"
                            >
                              <span aria-hidden="true">{{
                                collectionEmoji(b)
                              }}</span>
                              <span class="truncate">{{
                                collectionName(b)
                              }}</span>
                            </span>
                            <span aria-hidden="true" class="shrink-0">·</span>

                            <!-- 도메인 -->
                            <span class="min-w-0 truncate">{{
                              domain(b.url)
                            }}</span>
                            <span aria-hidden="true" class="shrink-0">·</span>

                            <!-- 수정일 -->
                            <time
                              class="shrink-0"
                              :datetime="b.updatedAt || ''"
                            >
                              {{ formatDate(b.updatedAt) }}
                            </time>
                          </div>
                        </div>
                      </div>
                    </div>
                  </li>

                  <li aria-hidden="true">
                    <div class="divider mx-6" />
                  </li>
                </template>
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

    <!-- 태그 교체 다이얼로그 -->
    <teleport to="#modals">
      <div
        v-if="showBulkReplaceDialog"
        class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
        @click.self="showBulkReplaceDialog = false"
        @keydown.esc="showBulkReplaceDialog = false"
      >
        <form
          class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6"
          role="dialog"
          aria-modal="true"
          aria-labelledby="tag-bulk-replace"
        >
          <header class="mb-4">
            <h3
              id="tag-bulk-replace"
              class="text-[17px] font-semibold leading-6"
            >
              태그 교체
            </h3>
            <p class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              선택한 {{ selectedCount }}개의 북마크에서<br />
              이 태그를
              <span class="font-semibold text-zinc-800 dark:text-zinc-100"
                >"{{ bulkReplaceTarget?.name }}"</span
              >(으)로 교체하시겠습니까?
            </p>
          </header>

          <footer class="mt-6 flex justify-end gap-2">
            <button
              type="button"
              class="px-4 py-2 border rounded-md text-sm hover:bg-accent"
              @click="showBulkReplaceDialog = false"
            >
              취소
            </button>
            <button
              ref="confirmBtnRef"
              type="button"
              :disabled="isTagMutating"
              class="px-4 py-2 rounded-md text-sm bg-blue-600 text-white hover:bg-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="handleBulkReplace"
            >
              교체
            </button>
          </footer>
        </form>
      </div>
    </teleport>

    <!-- 제거 확인 다이얼로그 -->
    <teleport to="#modals">
      <div
        v-if="showBulkDetachDialog"
        class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
        @click.self="showBulkDetachDialog = false"
        @keydown.esc="showBulkDetachDialog = false"
      >
        <div
          class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6"
          role="dialog"
          aria-modal="true"
          aria-labelledby="tag-bulk-detach"
        >
          <header class="mb-4">
            <h3
              id="tag-bulk-detach"
              class="text-[17px] font-semibold leading-6"
            >
              태그 제거
            </h3>
            <p class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              선택한 {{ selectedCount }}개의 북마크에서<br />
              이 태그를 제거하시겠습니까?
            </p>
          </header>

          <footer class="mt-6 flex justify-end gap-2">
            <button
              type="button"
              class="px-4 py-2 border rounded-md text-sm hover:bg-accent"
              @click="showBulkDetachDialog = false"
            >
              취소
            </button>
            <button
              ref="confirmBtnRef"
              type="button"
              :disabled="isTagMutating"
              class="px-4 py-2 rounded-md text-sm bg-red-600 text-white hover:bg-red-500 focus:outline-none focus:ring-2 focus:ring-red-500/40 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="handleBulkDetach"
            >
              제거
            </button>
          </footer>
        </div>
      </div>
    </teleport>
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
import { useBookmarkItemHelpers } from "@/composables/useBookmarkItemHelpers";
import { useFocusScroll } from "@/composables/useFocusScroll";
import { useBookmarkFavorite } from "@/composables/useBookmarkFavorite";
import { useTaggedBookmarkPagingScroll } from "@/composables/useTaggedBookmarkPagingScroll";

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
  if (isTagMutating.value) return;

  try {
    await taggedStore.detachFromBookmarks(selectedList.value);
    emit("tags-changed");
    clearSelection();
    showBulkDetachDialog.value = false;
  } catch (e) {
    toast.error(getErrorMessage(e, "태그 제거에 실패했습니다."));
  }
}

async function handleBulkReplace() {
  if (!props.tagId) return;
  if (selectedCount.value === 0) return;
  if (!canBulkReplace.value) return;
  if (isTagMutating.value) return;

  const target = bulkReplaceTarget.value;
  if (!target) return;

  try {
    await taggedStore.replaceOnBookmarks(Number(target.id), selectedList.value);
    emit("tags-changed");
    clearSelection();
    bulkTargetTagId.value = "";
    showBulkReplaceDialog.value = false;
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

const {
  canLoadMore,
  isLoadingMore,
  isEndReached,
  loadMore,
  cleanup,
  reconnect,
} = useTaggedBookmarkPagingScroll(listWrapRef, sentinelRef, {
  enabled: hasSelection,
  rootMargin: "300px",
  threshold: 0,
});

// ------------------------
// watchers
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

// ------------------------
// Dialogs
// ------------------------
const showBulkDetachDialog = ref(false);
const showBulkReplaceDialog = ref(false);
const confirmBtnRef = ref<HTMLButtonElement | null>(null);

const bulkReplaceTarget = computed(() => {
  const id = bulkTargetTagId.value;
  if (!id) return null;
  return (
    replaceCandidates.value.find((t) => String(t.id) === String(id)) ?? null
  );
});

function openBulkDetach() {
  if (props.tagId == null) return;
  if (selectedCount.value === 0) return;
  if (isTagMutating.value) return;

  showBulkDetachDialog.value = true;
  nextTick(() => requestAnimationFrame(() => confirmBtnRef.value?.focus()));
}

function openBulkReplace() {
  if (props.tagId == null) return;
  if (selectedCount.value === 0) return;
  if (!canBulkReplace.value) return;
  if (!bulkReplaceTarget.value) return;
  if (isTagMutating.value) return;

  showBulkReplaceDialog.value = true;
  nextTick(() => requestAnimationFrame(() => confirmBtnRef.value?.focus()));
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
