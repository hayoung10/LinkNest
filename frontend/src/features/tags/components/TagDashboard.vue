<template>
  <section class="h-full min-h-0 overflow-y-auto bg-background">
    <!-- 선택 없음 -->
    <BaseEmpty
      v-if="tagId == null"
      title="태그를 선택해주세요."
      description="왼쪽 목록에서 태그를 선택하면 상세 정보를 볼 수 있습니다."
    />

    <template v-else>
      <!-- 헤더 -->
      <TagHeader
        variant="dashboard"
        :title="tag?.name ?? '태그'"
        :subtitle="`${tag?.bookmarkCount ?? 0}개의 북마크에서 사용 중`"
      >
        <template #left>
          <div
            class="size-14 rounded-2xl bg-gradient-to-br from-violet-500 to-blue-500 dark:from-violet-400 dark:to-blue-400 grid place-items-center"
          >
            <TagIcon
              :size="26"
              :strokeWidth="2"
              klass="text-white"
              aria-hidden="true"
            />
          </div>
        </template>
      </TagHeader>

      <div class="px-6 py-6 space-y-8">
        <!-- 기본 정보 -->
        <section>
          <h3 class="text-base font-semibold">기본 정보</h3>

          <div class="mt-4 grid gap-4 lg:grid-cols-2">
            <!-- 이름 -->
            <div class="rounded-2xl border border-border bg-card p-5">
              <div class="flex items-center gap-3">
                <div
                  class="size-10 rounded-xl bg-zinc-300 dark:bg-zinc-600 grid place-items-center"
                >
                  <span
                    class="text-xl font-bold text-zinc-700 dark:text-zinc-200"
                    >#</span
                  >
                </div>
                <div class="min-w-0">
                  <div class="text-xs text-zinc-500 dark:text-zinc-400">
                    태그 이름
                  </div>
                  <div class="mt-1 text-base font-semibold truncate">
                    {{ tag?.name ?? "-" }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 날짜 -->
            <div class="grid gap-4 sm:grid-cols-2">
              <!-- 생성일 -->
              <div class="rounded-2xl border border-border bg-card p-5">
                <div class="text-xs text-zinc-500 dark:text-zinc-400">
                  생성일
                </div>
                <div class="mt-2 text-sm font-semibold tabular-nums">
                  {{ createdAtText }}
                </div>
              </div>

              <!-- 수정일 -->
              <div class="rounded-2xl border border-border bg-card p-5">
                <div class="text-xs text-zinc-500 dark:text-zinc-400">
                  수정일
                </div>
                <div class="mt-2 text-sm font-semibold tabular-nums">
                  {{ updatedAtText }}
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 상태 -->
        <section>
          <h3 class="text-base font-semibold">상태</h3>

          <div class="mt-4 rounded-2xl border border-border bg-card p-6">
            <div class="flex items-start justify-between gap-4">
              <div>
                <div class="text-sm text-zinc-500 dark:text-zinc-400">
                  사용 중인 북마크
                </div>
                <div class="mt-2 text-4xl font-bold tabular-nums">
                  {{ tag?.bookmarkCount ?? 0 }}
                </div>
              </div>

              <div
                class="size-14 rounded-2xl bg-zinc-300 dark:bg-zinc-600 grid place-items-center"
              >
                <TagIcon :size="26" :strokeWidth="2" aria-hidden="true" />
              </div>
            </div>

            <div class="mt-6 h-px bg-zinc-200/70 dark:bg-zinc-700/50"></div>

            <!-- 사용률 -->
            <div class="mt-6">
              <div class="flex items-center justify-between text-sm">
                <span class="text-zinc-500 dark:text-zinc-400">사용률</span>
                <span class="tabular-nums">{{ usagePercentLabel }}</span>
              </div>
              <div
                class="mt-2 h-2 rounded-full bg-zinc-200 dark:bg-zinc-700 overflow-hidden"
              >
                <div
                  class="h-2.5 rounded-full bg-emerald-500/90 transition-all"
                  :class="usagePercent === 0 && 'opacity-0'"
                  :style="{ width: usageBarWidth }"
                />
              </div>
            </div>
          </div>
        </section>

        <!-- 빠른 작업 -->
        <section>
          <h3 class="text-base font-semibold">빠른 작업</h3>

          <div class="mt-4 grid gap-4 md:grid-cols-3">
            <button
              type="button"
              class="group rounded-2xl border border-border bg-card p-4 text-center transition-all hover:bg-accent hover:border-zinc-300/70 dark:hover:border-zinc-600/60 hover:shadow-md"
              @click="openRename"
            >
              <div
                class="mx-auto size-12 rounded-xl bg-blue-100 dark:bg-blue-500/20 grid place-items-center text-xl transition-colors group-hover:bg-blue-200 dark:group-hover:bg-blue-500/30"
              >
                ✏️
              </div>
              <div class="mt-4 font-semibold">이름 변경</div>
              <div class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
                태그 이름 수정
              </div>
            </button>

            <button
              type="button"
              class="group rounded-2xl border border-border bg-card p-4 text-center transition-all hover:bg-accent hover:border-zinc-300/70 dark:hover:border-zinc-600/60 hover:shadow-md"
              @click="openMerge"
            >
              <div
                class="mx-auto size-12 rounded-xl bg-violet-100 dark:bg-violet-500/20 grid place-items-center text-xl transition-colors group-hover:bg-violet-200 dark:group-hover:bg-violet-500/30"
              >
                ⤴️
              </div>
              <div class="mt-4 font-semibold">태그 병합</div>
              <div class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
                다른 태그와 병합
              </div>
            </button>

            <button
              type="button"
              class="group rounded-2xl border border-border bg-card p-4 text-center transition-all hover:bg-accent hover:border-zinc-300/70 dark:hover:border-zinc-600/60 hover:shadow-md"
              @click="openDelete"
            >
              <div
                class="mx-auto size-12 rounded-xl bg-red-100 dark:bg-red-500/20 grid place-items-center text-xl transition-colors group-hover:bg-red-200 dark:group-hover:bg-red-500/30"
              >
                🗑️
              </div>
              <div class="mt-4 font-semibold">태그 삭제</div>
              <div class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
                영구적으로 삭제
              </div>
            </button>
          </div>
        </section>

        <!-- 사용 중인 북마크 -->
        <section>
          <div class="flex items-center justify-between gap-4">
            <h3 class="text-base font-semibold">사용 중인 북마크</h3>

            <button
              v-if="hasMore"
              type="button"
              class="inline-flex items-center gap-2 px-3 py-2 rounded-md border border-border text-sm hover:bg-accent hover:border-zinc-300/70 dark:hover:border-zinc-600/60 hover:shadow-md transition-all"
              @click="onClickOpenAll"
            >
              모두 보기 ({{ tag?.bookmarkCount ?? 0 }})
              <span aria-hidden="true">→</span>
            </button>
          </div>

          <div class="mt-4 rounded-2xl border border-border bg-card p-4">
            <div v-if="previewLoading" class="space-y-3">
              <div
                v-for="i in 4"
                :key="i"
                class="h-12 rounded-xl bg-zinc-100 dark:bg-zinc-800 animate-pulse"
              />
            </div>

            <div v-else-if="previewError" class="py-10 text-center">
              <div class="text-sm text-zinc-500 dark:text-zinc-400">
                북마크 미리보기를 불러오지 못했습니다.
              </div>
            </div>

            <div
              v-else-if="previewItems.length === 0"
              class="py-10 text-center"
            >
              <div class="text-sm text-zinc-500 dark:text-zinc-400">
                아직 이 태그가 붙은 북마크가 없습니다.
              </div>
            </div>

            <ul
              v-else
              class="divide-y divide-zinc-200/70 dark:divide-zinc-700/50"
            >
              <li v-for="b in previewItems" :key="b.id">
                <button
                  type="button"
                  class="w-full flex items-center gap-3 p-3 rounded-xl text-left hover:bg-zinc-100/70 dark:hover:bg-zinc-800/60 transition-colors"
                  @click="onClickPreviewItem(b.id)"
                >
                  <!-- 썸네일/파비콘 영역 -->
                  <div
                    class="size-10 rounded-xl bg-zinc-200 dark:bg-zinc-700 shrink-0 overflow-hidden grid place-items-center"
                  >
                    <span
                      class="text-base leading-none text-zinc-700 dark:text-zinc-200"
                      >{{ b.emoji || "🔖" }}</span
                    >
                  </div>

                  <div class="min-w-0 flex-1">
                    <div class="font-semibold truncate">
                      {{ b.title ?? b.url }}
                    </div>
                    <div
                      class="text-xs text-zinc-500 dark:text-zinc-400 truncate"
                    >
                      {{ b.url }}
                    </div>
                  </div>

                  <span class="text-zinc-400 dark:text-zinc-500">→</span>
                </button>
              </li>
            </ul>
          </div>
        </section>
      </div>
    </template>

    <!-- 이름 변경 다이얼로그 -->
    <teleport to="#modals">
      <div
        v-if="showRenameDialog"
        class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
        @click.self="showRenameDialog = false"
        @keydown.esc="showRenameDialog = false"
      >
        <form
          class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6"
          role="dialog"
          aria-modal="true"
          aria-labelledby="tag-rename"
          @submit.prevent="handleRename"
        >
          <header class="mb-4">
            <h3 id="tag-rename" class="text-[17px] font-semibold leading-6">
              이름 변경
            </h3>
            <p class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              태그의 이름을 변경합니다.
            </p>
          </header>

          <div class="my-4 h-px bg-zinc-200/80 dark:bg-zinc-700/60"></div>

          <div class="space-y-2">
            <label class="block text-sm">태그 이름 *</label>
            <input
              ref="nameInputRef"
              v-model="renameName"
              type="text"
              class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
              required
            />
          </div>

          <footer class="mt-6 flex justify-end gap-2">
            <button
              type="button"
              class="px-4 py-2 rounded-md text-sm border border-zinc-300/70 dark:border-zinc-600/60 bg-zinc-100/70 dark:bg-zinc-800/70 hover:bg-zinc-100 dark:hover:bg-zinc-800"
              @click="showRenameDialog = false"
            >
              취소
            </button>
            <button
              type="submit"
              :disabled="!canRename || isMutating.rename"
              class="px-4 py-2 rounded-md text-sm text-white bg-blue-600 hover:bg-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              변경
            </button>
          </footer>
        </form>
      </div>
    </teleport>

    <!-- 태그 병합 다이얼로그 -->
    <teleport to="#modals">
      <div
        v-if="showMergeDialog"
        class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
        @click.self="showMergeDialog = false"
        @keydown.esc="showMergeDialog = false"
      >
        <form
          class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6"
          role="dialog"
          aria-modal="true"
          aria-labelledby="tag-merge"
          @submit.prevent="handleMerge"
        >
          <header class="mb-4">
            <h3 id="tag-merge" class="text-[17px] font-semibold leading-6">
              태그 병합
            </h3>
            <p class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              현재 태그를 다른 태그로 병합합니다. (현재 태그는 사라집니다)
            </p>
          </header>

          <div class="my-4 h-px bg-zinc-200/80 dark:bg-zinc-700/60"></div>

          <div class="space-y-2">
            <label class="block text-sm">병합 대상 태그</label>
            <select
              ref="mergeSelectRef"
              v-model="mergeTargetId"
              type="text"
              class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 focus:outline-none focus:ring-2 focus:ring-violet-500/30"
              required
            >
              <option value="" disabled>선택하세요</option>
              <option v-for="t in mergeCandidates" :key="t.id" :value="t.id">
                {{ t.name }} ({{ t.bookmarkCount }})
              </option>
            </select>
          </div>

          <footer class="mt-6 flex justify-end gap-2">
            <button
              type="button"
              class="px-4 py-2 rounded-md text-sm border border-zinc-300/70 dark:border-zinc-600/60 bg-zinc-100/70 dark:bg-zinc-800/70 hover:bg-zinc-100 dark:hover:bg-zinc-800"
              @click="showMergeDialog = false"
            >
              취소
            </button>
            <button
              type="submit"
              :disabled="!canMerge || isMutating.merge"
              class="px-4 py-2 rounded-md text-sm text-white bg-violet-600 hover:bg-violet-500 focus:outline-none focus:ring-2 focus:ring-violet-500/40 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              병합
            </button>
          </footer>
        </form>
      </div>
    </teleport>

    <!-- 삭제 확인 다이얼로그 -->
    <teleport to="#modals">
      <div
        v-if="showDeleteDialog"
        class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
        @click.self="showDeleteDialog = false"
        @keydown.esc="showDeleteDialog = false"
      >
        <div
          class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6"
          role="dialog"
          aria-modal="true"
          aria-labelledby="tag-delete"
        >
          <header class="mb-4">
            <h3 id="tag-delete" class="text-[17px] font-semibold leading-6">
              태그 삭제
            </h3>
            <p class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              정말로 이 태그를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.
            </p>
            <p class="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              <span class="font-bold"
                >{{ tag?.bookmarkCount ?? 0 }}개의 북마크</span
              >에서 이 태그가 제거됩니다.
            </p>
          </header>

          <footer class="mt-6 flex justify-end gap-2">
            <button
              type="button"
              class="px-4 py-2 rounded-md text-sm border border-zinc-300/70 dark:border-zinc-600/60 bg-zinc-100/70 dark:bg-zinc-800/70 hover:bg-zinc-100 dark:hover:bg-zinc-800"
              @click="showDeleteDialog = false"
            >
              취소
            </button>
            <button
              ref="confirmBtnRef"
              type="button"
              :disabled="isMutating.delete"
              class="px-4 py-2 rounded-md text-sm text-white bg-red-600 hover:bg-red-500 focus:outline-none focus:ring-2 focus:ring-red-500/40 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="handleDelete"
            >
              삭제
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
import { useTagsStore } from "@/stores/tags";
import { useToastStore } from "@/stores/toast";
import { BaseEmpty } from "@/components/ui";
import TagIcon from "@/components/icons/TagIcon.vue";
import { getErrorMessage } from "@/utils/errorMessage";
import { TagHeader } from "@/features/tags";
import * as TagApi from "@/api/tags";

defineOptions({ name: "TagDashboard" });

const props = defineProps<{ tagId: ID | null }>();

const emit = defineEmits<{
  (e: "open-bookmarks", payload?: { tagId: ID; focusBookmarkId?: ID }): void;
  (e: "open-settings"): void;
  (e: "merge", tagId: ID): void;
  (e: "delete", tagId: ID): void;
}>();

const toast = useToastStore();
const tagsStore = useTagsStore();

const { items, totalBookmarks, isMutating } = storeToRefs(tagsStore);

const tag = computed(() => {
  if (props.tagId === null) return null;
  return items.value.find((t) => t.id === props.tagId) ?? null;
});

// ------------------------
// 날짜
// ------------------------
const createdAtText = computed(() => {
  const c = tag.value?.createdAt;
  if (!c) return "-";
  return new Date(c).toLocaleDateString("ko-KR");
});
const updatedAtText = computed(() => {
  const u = tag.value?.updatedAt;
  if (!u) return "-";
  return new Date(u).toLocaleDateString("ko-KR");
});

// ------------------------
// 사용률
// ------------------------
const usagePercent = computed(() => {
  const total = totalBookmarks.value ?? 0;
  const used = tag.value?.bookmarkCount ?? 0;
  if (total <= 0) return 0;
  return (used / total) * 100;
});
const usagePercentLabel = computed(() => `${usagePercent.value.toFixed(1)}%`);
const usageBarWidth = computed(
  () => `${Math.min(100, Math.max(0, usagePercent.value))}%`,
);

// ------------------------
// 북마크 미리보기
// ------------------------
type BookmarkPreview = {
  id: ID;
  title?: string | null;
  url: string;
};

const PREVIEW_LIMIT = 5;

const previewItems = ref<TaggedBookmark[]>([]);
const previewLoading = ref(false);
const previewError = ref<unknown | null>(null);

const hasPreview = computed(() => previewItems.value.length > 0);
const hasMore = computed(() => {
  const cnt = tag.value?.bookmarkCount ?? 0;
  return cnt > PREVIEW_LIMIT;
});

async function loadPreview(tagId: ID) {
  previewLoading.value = true;
  previewError.value = null;
  try {
    const res = await TagApi.getTaggedBookmarks(tagId, {
      page: 0,
      size: PREVIEW_LIMIT,
    });
    previewItems.value = res.items ?? [];
  } catch (e) {
    previewError.value = e;
    previewItems.value = [];
  } finally {
    previewLoading.value = false;
  }
}

async function safeReloadPreview(tagId: ID) {
  try {
    await loadPreview(tagId);
  } catch (e) {
    toast.error(getErrorMessage(e, "미리보기를 불러오지 못했습니다."));
  }
}

watch(
  () => props.tagId,
  (id) => {
    if (!id) {
      previewItems.value = [];
      previewError.value = null;
      previewLoading.value = false;
      return;
    }
    safeReloadPreview(Number(id));
  },
  { immediate: true },
);

function onClickPreviewItem(bookmarkId: ID) {
  if (props.tagId == null) return;
  emit("open-bookmarks", { tagId: props.tagId, focusBookmarkId: bookmarkId });
}

function onClickOpenAll() {
  if (props.tagId == null) return;
  emit("open-bookmarks", { tagId: props.tagId });
}

// ------------------------
// Dialogs
// ------------------------
const showRenameDialog = ref(false);
const showMergeDialog = ref(false);
const showDeleteDialog = ref(false);

const renameInputRef = ref<HTMLInputElement | null>(null);
const mergeSelectRef = ref<HTMLSelectElement | null>(null);
const deleteConfirmBtnRef = ref<HTMLButtonElement | null>(null);

// rename
const renameName = ref("");
const canRename = computed(() => renameName.value.trim().length > 0);

// merge
const mergeTargetId = ref<ID | "">("");
const canMerge = computed(() => {
  if (!props.tagId) return false;
  if (!mergeTargetId.value) return false;
  return String(mergeTargetId.value) !== String(props.tagId);
});
const mergeCandidates = computed(() => {
  if (!props.tagId) return [];
  return items.value.filter((t) => t.id !== props.tagId);
});

function openRename() {
  if (!tag.value || props.tagId == null) return;
  renameName.value = tag.value.name;
  showRenameDialog.value = true;
  nextTick(() => {
    renameInputRef.value?.focus();
    renameInputRef.value?.select();
  });
}

async function handleRename() {
  if (props.tagId == null) return;
  if (!canRename.value || isMutating.value.rename) return;

  try {
    await tagsStore.rename(props.tagId, { name: renameName.value });
    showRenameDialog.value = false;
  } catch (e) {
    toast.error(getErrorMessage(e, "이름 변경에 실패했습니다."));
  }
}

function openMerge() {
  if (props.tagId == null) return;
  mergeTargetId.value = "";
  showMergeDialog.value = true;
  nextTick(() => mergeSelectRef.value?.focus());
}

async function handleMerge() {
  if (props.tagId == null) return;
  if (!canMerge.value || isMutating.value.merge) return;

  try {
    await tagsStore.merge(props.tagId, {
      targetTagId: Number(mergeTargetId.value),
    });
    showMergeDialog.value = false;
    emit("merge", props.tagId);
  } catch (e) {
    toast.error(getErrorMessage(e, "태그 병합에 실패했습니다."));
  }
}

function openDelete() {
  showDeleteDialog.value = true;
  nextTick(() => deleteConfirmBtnRef.value?.focus());
}

async function handleDelete() {
  if (props.tagId == null) return;
  if (isMutating.value.delete) return;

  try {
    await tagsStore.delete(props.tagId);
    showDeleteDialog.value = false;
    emit("delete", props.tagId);
  } catch (e) {
    toast.error(getErrorMessage(e, "태그 삭제에 실패했습니다."));
  }
}
</script>
