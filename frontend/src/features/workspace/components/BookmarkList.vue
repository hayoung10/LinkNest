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
      <header class="flex items-center justify-between mb-2">
        <div class="min-w-0 pl-3">
          <!-- path -->
          <div
            class="mb-1 ml-0.5 min-h-[16px] text-xs text-zinc-500 flex items-center gap-1 min-w-0"
          >
            <template v-if="isLoadingPath">
              <span
                class="inline-block h-3 w-28 rounded bg-zinc-200/70 animate-pulse"
              />
            </template>

            <template v-else-if="cPath.length">
              <template v-for="(p, idx) in cPath" :key="p.id">
                <span class="truncate">
                  <span v-if="p.emoji" class="mr-1">{{ p.emoji }}</span>
                  {{ p.name }}
                </span>
                <span v-if="idx < cPath.length - 1" class="opacity-60">/</span>
              </template>
            </template>
          </div>

          <div class="min-w-0 flex items-center gap-2">
            <span class="shrink-0 text-muted-foreground opacity-80">
              <template v-if="collection?.emoji">
                <span class="text-[20px] leading-none">{{
                  collection.emoji
                }}</span>
              </template>
              <template v-else>
                <FolderIcon size="22" />
              </template>
            </span>

            <!-- 컬렉션 이름 -->
            <h2 class="text-xl font-semibold text-foreground truncate">
              {{ collection?.name ?? "컬렉션" }}
            </h2>
          </div>
        </div>

        <div class="flex text-center gap-2 pr-3">
          <button
            type="button"
            :disabled="isAddDisabled"
            class="inline-flex items-center gap-1 px-3.5 py-1.5 rounded-md bg-neutral-900 text-white hover:bg-neutral-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-neutral-900"
            aria-label="새 북마크 추가"
            @click="$emit('open-add')"
          >
            <PlusIcon :size="16" klass="shrink-0" />
            <span>추가</span>
          </button>
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
        title="북마크를 추가하세요."
        description="오른쪽 위 '추가' 버튼으로 링크를 추가하세요."
      />

      <!-- 북마크 리스트 -->
      <template v-else>
        <div class="flex-1 min-h-0 overflow-y-auto pr-1">
          <ul class="mt-0" role="list" aria-label="북마크 목록">
            <template v-for="b in bookmarks" :key="b.id">
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
          {{ bookmarks.length }} 북마크
        </footer>
      </template>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import type { Bookmark, CollectionNode, ID } from "@/types/common";
import FolderIcon from "@/components/icons/FolderIcon.vue";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import BookmarkIcon from "@/components/icons/BookmarkIcon.vue";
import StarIcon from "@/components/icons/StarIcon.vue";
import { useWorkspaceStore } from "@/stores/workspace";
import { storeToRefs } from "pinia";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";
import * as CollectionApi from "@/api/collections";
import { CollectionPathRes } from "@/api/types";
import { useToastStore } from "@/stores/toast";

const props = defineProps<{
  collection: CollectionNode | null;
  selectedBookmarkId?: ID | null;
}>();

const emit = defineEmits<{
  (e: "open-add"): void;
  (e: "select-bookmark", id: ID): void;
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
const isEmpty = computed(
  () =>
    hasSelection.value &&
    !isLoadingBookmarks.value &&
    !hasError.value &&
    bookmarks.value.length === 0
);

const isAddDisabled = computed(
  () =>
    !hasSelection.value ||
    isLoadingBookmarks.value ||
    hasError.value ||
    isMutating.value.createBookmark
);

const path = ref<CollectionPathRes[]>([]);
const isLoadingPath = ref(false);
const cPath = computed(() => path.value.slice(0, -1));

function onRetry() {
  const cid = selectedCollectionId.value;
  if (cid != null) workspace.fetchBookmarks(cid);
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

watch(
  [() => selectedCollectionId.value, () => collectionNodes.value],
  ([cid]) => refreshPath(cid),
  { immediate: true, deep: true }
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
