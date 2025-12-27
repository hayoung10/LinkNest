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
        <div class="min-w-0 flex items-center gap-2 pl-3">
          <FolderIcon size="28" class="text-muted-foreground opacity-80" />
          <h2 class="text-xl font-semibold text-foreground truncate">
            {{ collection?.name ?? "컬렉션" }}
          </h2>
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

      <!-- 북마크 카드 -->
      <template v-else>
        <div class="flex-1 min-h-0 overflow-y-auto pr-1">
          <div
            class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 auto-rows-fr"
            aria-label="북마크 카드 목록"
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
                  <span class="truncate">{{ domain(b.url) }}</span>
                  <time :datetime="b.createdAt || ''">
                    {{ formatDate(b.createdAt) }}
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
import { computed } from "vue";
import type { Bookmark, Collection, ID } from "@/types/common";
import FolderIcon from "@/components/icons/FolderIcon.vue";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import BookmarkIcon from "@/components/icons/BookmarkIcon.vue";
import { useWorkspaceStore } from "@/stores/workspace";
import { storeToRefs } from "pinia";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";

const props = defineProps<{
  collection: Collection | null;
  selectedBookmarkId?: ID | null;
}>();

const emit = defineEmits<{
  (e: "open-add"): void;
  (e: "select-bookmark", id: ID): void;
}>();

const workspace = useWorkspaceStore();
const { selectedCollectionId, bookmarks, isLoading, error, isMutating } =
  storeToRefs(workspace);

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

const hasSelection = computed(() => selectedCollectionId.value != null);
const isAddDisabled = computed(
  () =>
    !hasSelection.value ||
    isLoadingBookmarks.value ||
    hasError.value ||
    isMutating.value.createBookmark
);

function onRetry() {
  const cid = selectedCollectionId.value;
  if (cid != null) workspace.fetchBookmarks(cid);
}

// 유틸
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
</script>

<style scoped>
.divider {
  height: 1px;
  margin: 6px 4px;
  background: color-mix(in oklab, currentColor 12%, transparent);
  border-radius: 1px;
}
</style>
