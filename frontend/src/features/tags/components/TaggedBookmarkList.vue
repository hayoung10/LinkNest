<template>
  <section class="h-full min-h-0 overflow-y-auto bg-background">
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

      <div class="px-6 py-6">
        <div class="w-full max-w-4xl mx-auto min-h-0">
          <!-- 상태 분기 -->
          <BaseError
            v-if="hasError"
            title="북마크를 불러올 수 없습니다"
            :description="errorMessage"
            :onRetry="onRetry"
          />

          <BaseLoading
            v-else-if="isLoadingBookmarks"
            label="북마크를 불러오는 중…"
          />

          <BaseEmpty
            v-else-if="isEmpty"
            title="북마크가 없습니다."
            description="이 태그가 연결된 북마크가 없습니다."
          />

          <!-- 북마크 리스트 -->
          <template v-else>
            <div ref="listWrapRef" class="min-h-0 overflow-y-auto pr-1">
              <ul class="mt-0" role="list" aria-label="태그 북마크 리스트 목록">
                <template v-for="b in items" :key="b.id">
                  <li
                    :data-bid="String(b.id)"
                    class="group px-2 py-3 rounded-md cursor-pointer select-none transition-colors"
                    :class="[
                      isActive(b)
                        ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-100 ring-1 ring-blue-300'
                        : isFocused(b)
                          ? 'bg-amber-50 ring-1 ring-amber-300 dark:bg-amber-900/20'
                          : 'hover:bg-zinc-100 dark:hover:bg-zinc-800',
                    ]"
                  >
                    <button
                      type="button"
                      class="w-full text-left"
                      @click="onSelect(b)"
                      :title="displayTitle(b)"
                    >
                      <div class="flex items-stretch gap-3 px-3">
                        <!-- favorite (TODO: disabled) -->
                        <div class="shrink-0 pt-2">
                          <button
                            type="button"
                            class="shrink-0 inline-flex items-center justify-center size-8 rounded-md bg-white border border-zinc-300 shadow-sm hover:bg-zinc-50 dark:bg-zinc-900 dark:border-zinc-600 dark:hover:bg-zinc-800 transition-colors"
                            :disabled="true"
                            title="태그 화면에서는 즐겨찾기 변경을 지원하지 않습니다."
                            :aria-label="
                              b.isFavorite ? '즐겨찾기 해제' : '즐겨찾기 추가'
                            "
                            @click.stop.prevent
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
                                  align="right"
                                  @open-workspace="onOpenWorkspace(b)"
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
import { useTaggedBookmarksStore } from "@/stores/taggedBookmarks";
import { useWorkspaceStore } from "@/stores/workspace";
import { useToastStore } from "@/stores/toast";
import { TaggedBookmarkMoreMenu, TagHeader } from "@/features/tags";

const props = defineProps<{
  tagId: ID | null;
  selectedBookmarkId?: ID | null;
  focusBookmarkId?: ID | null;
}>();

const emit = defineEmits<{
  (e: "select-bookmark", id: ID): void;
  (e: "back"): void;
  (e: "open-settings"): void;
}>();

const toast = useToastStore();
const workspace = useWorkspaceStore();
const tagged = useTaggedBookmarksStore();

const { items, isLoading, error, loaded } = storeToRefs(tagged);

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

function onRetry() {
  tagged.safeReload();
}

function isFavoriteMutating(id: ID) {
  return true;
}

async function onToggleFavorite(b: TaggedBookmark) {
  // TODO: tagged bookmarks 전용 toggle API 연결 후 활성화
  return;
}

function displayTitle(b: TaggedBookmark): string {
  const t = (b.title ?? "").trim();
  return t || "(제목 없음)";
}

function hasTitle(b: TaggedBookmark): boolean {
  return !!b.title?.trim();
}

function isActive(b: TaggedBookmark): boolean {
  return props.selectedBookmarkId === b.id;
}

function tagCount(b: TaggedBookmark) {
  return b.tags?.length ?? 0;
}

function extraTagCount(b: TaggedBookmark, visible = 3) {
  return Math.max(tagCount(b) - visible, 0);
}

function visibleTags(b: TaggedBookmark, visible = 3) {
  return b.tags?.slice(0, visible) ?? [];
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

function coverUrl(b: TaggedBookmark): string | null {
  if (b.imageMode === "CUSTOM") return b.customImageUrl ?? null;
  if (b.imageMode === "AUTO") return b.autoImageUrl ?? null;
  return null;
}

function isAutoPending(b: TaggedBookmark): boolean {
  return b.imageMode === "AUTO" && !b.autoImageUrl;
}

function onSelect(b: TaggedBookmark) {
  emit("select-bookmark", b.id);
}

function onOpenWorkspace(b: TaggedBookmark) {
  // TODO: 라우팅 연결 예정
  console.log("[TaggedBookmarkCard] open workspace:", b.id);
}

const listWrapRef = ref<HTMLElement | null>(null);

function isFocused(b: TaggedBookmark): boolean {
  return props.focusBookmarkId === b.id;
}

async function scrollToFocus() {
  const id = props.focusBookmarkId;
  if (!id) return;

  await nextTick();
  const el = listWrapRef.value?.querySelector<HTMLElement>(
    `[data-bid="${id}"]`,
  );
  if (!el) return;

  el.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

watch(
  () => props.focusBookmarkId,
  () => scrollToFocus(),
  { immediate: true },
);

watch(
  () => props.tagId,
  async (next) => {
    tagged.setContext(next);
    if (next != null) {
      try {
        await tagged.load(true);
        await scrollToFocus();
      } catch {
        // 화면에서 BaseError가 처리
      }
    }
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
