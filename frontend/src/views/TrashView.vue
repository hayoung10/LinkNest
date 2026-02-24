<template>
  <div class="h-screen w-full bg-background flex flex-col">
    <!-- 상단 헤더 -->
    <PageHeader
      title="휴지통"
      subtitle="삭제된 항목을 복구하거나 영구 삭제할 수 있습니다."
      :show-tag-management="false"
      :show-trash="true"
      @back="goBack"
      @open-tag-management="onOpenTagManagement"
      @open-settings="onOpenSettings"
      @logout="onLogout"
    />

    <!-- 본문 -->
    <main class="flex-1 min-h-0 overflow-hidden">
      <section class="h-full min-h-0 flex flex-col">
        <!-- 상단 탭 + 비우기 -->
        <div class="px-6 py-4 border-b border-border flex items-center gap-3">
          <div
            class="inline-flex rounded-xl bg-zinc-100/70 dark:bg-zinc-800/50 p-1"
          >
            <button
              type="button"
              class="px-3 py-2 text-sm rounded-lg transition-colors"
              :class="tabClass(null)"
              @click="setType(null)"
            >
              전체
            </button>
            <button
              type="button"
              class="px-3 py-2 text-sm rounded-lg transition-colors"
              :class="tabClass('COLLECTION')"
              @click="setType('COLLECTION')"
            >
              컬렉션
            </button>
            <button
              type="button"
              class="px-3 py-2 text-sm rounded-lg transition-colors"
              :class="tabClass('BOOKMARK')"
              @click="setType('BOOKMARK')"
            >
              북마크
            </button>
            <button
              type="button"
              class="px-3 py-2 text-sm rounded-lg transition-colors"
              :class="tabClass('TAG')"
              @click="setType('TAG')"
            >
              태그
            </button>
          </div>

          <div class="flex-1" />

          <!-- 비우기 -->
          <button
            type="button"
            class="inline-flex items-center gap-2 px-3 py-1 rounded-md text-sm transition-colors disabled:opacity-50 bg-red-50 text-red-700 border border-red-300 hover:bg-red-100 dark:bg-red-950/30 dark:border-red-900/40 dark:text-red-300 dark:hover:bg-red-950/50"
            :disabled="trash.isMutating.empty || isLoadingTrash"
            @click="onEmpty"
          >
            <span aria-hidden="true">🗑️</span>휴지통 비우기
          </button>
        </div>

        <!-- 목록 영역 -->
        <div ref="listWrapRef" class="flex-1 min-h-0 overflow-auto">
          <!-- 상태 분기 -->
          <BaseError
            v-if="hasError"
            title="휴지통을 불러올 수 없습니다."
            :onRetry="onRetry"
          />
          <BaseLoading
            v-else-if="isLoadingTrash && trash.items.length === 0"
            label="불러오는 중…"
          />
          <BaseEmpty
            v-else-if="trash.items.length === 0"
            title="휴지통이 비어 있습니다."
          />

          <!-- 카드 그리드 + 선택 툴바 -->
          <div v-else class="p-6 space-y-4">
            <div
              v-if="selectedCount > 0"
              class="sticky top-0 z-20 bg-background/95 backdrop-blur pb-3"
            >
              <TrashSelectionToolbar
                :selected-count="selectedCount"
                :all-checked="allChecked"
                :is-mutating="isBulkMutating"
                @toggle-all="toggleAllVisible"
                @restore="onRestoreSelected"
                @delete="onDeleteSelected"
                @clear="clearSelection"
              />
            </div>

            <div
              class="grid gap-4"
              style="
                grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
              "
            >
              <article
                v-for="item in trash.items"
                :key="keyOf(item)"
                :class="[
                  'relative rounded-xl border bg-background transition-all duration-150 ease-out hover:shadow-sm',
                  isSelected(item)
                    ? 'ring-2 ring-zinc-300 border-zinc-400 dark:ring-zinc-400 dark:border-zinc-300 shadow-md scale-[1.01]'
                    : 'border-border',
                ]"
              >
                <div
                  class="absolute top-0 left-0 h-1 w-full"
                  :class="typeBarClass(item.type)"
                />

                <div class="p-4 flex gap-3">
                  <!-- 체크 -->
                  <div class="pt-1">
                    <input
                      type="checkbox"
                      class="size-4"
                      :checked="isSelected(item)"
                      @change="toggleSelected(item)"
                      :aria-label="`선택: ${item.title}`"
                    />
                  </div>

                  <!-- 아이콘 -->
                  <div
                    class="size-10 shrink-0 rounded-lg border border-border grid place-items-center text-lg"
                  >
                    <span>{{ item.emoji ?? fallbackEmoji(item.type) }}</span>
                  </div>

                  <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-2 min-w-0">
                      <span
                        class="text-xs px-2 py-0.5 rounded-md border"
                        :class="typeBadgeClass(item.type)"
                        >{{ typeLabel(item.type) }}</span
                      >
                      <h3 class="font-semibold truncate">{{ item.title }}</h3>
                    </div>

                    <p
                      v-if="item.subtitle"
                      class="mt-1 text-xs text-neutral-500 dark:text-neutral-400 truncate"
                    >
                      {{ item.subtitle }}
                    </p>

                    <p
                      v-if="item.parentName"
                      class="mt-1 text-xs text-neutral-500 dark:text-neutral-400 truncate"
                    >
                      상위: {{ item.parentEmoji ?? "" }} {{ item.parentName }}
                    </p>

                    <p
                      class="mt-2 text-xs text-neutral-500 dark:text-neutral-400"
                    >
                      <template v-if="item.type === 'COLLECTION'">
                        하위 {{ item.childCount ?? 0 }} · 북마크
                        {{ item.bookmarkCount ?? 0 }}
                      </template>
                      <template v-else-if="item.type === 'TAG'"
                        >연결된 북마크 {{ item.taggedCount ?? 0 }}</template
                      >
                      <template v-else>&nbsp;</template>
                    </p>

                    <div class="mt-2 flex items-center gap-2 text-xs">
                      <span class="text-neutral-500 dark:text-neutral-400"
                        >삭제: {{ formatRelative(item.deletedAt) }}</span
                      >
                      <span class="text-orange-600"
                        >· {{ daysLeftText(item.deletedAt) }}</span
                      >
                    </div>
                  </div>
                </div>

                <div class="px-4 pb-4 flex items-center gap-2">
                  <button
                    type="button"
                    class="flex-1 inline-flex justify-center items-center gap-2 px-3 py-2 rounded-md border border-border text-sm hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-50"
                    :disabled="trash.isMutating.restore"
                    @click="onRestore(item.type, item.id)"
                  >
                    ↩️ 복원
                  </button>

                  <button
                    type="button"
                    class="flex-1 inline-flex justify-center items-center gap-2 px-3 py-2 rounded-md border border-border text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-950/30 disabled:opacity-50"
                    :disabled="trash.isMutating.delete"
                    @click="onDelete(item.type, item.id)"
                  >
                    🗑️ 영구 삭제
                  </button>
                </div>
              </article>
            </div>
          </div>

          <!-- 더 보기 -->
          <div class="py-2 flex justify-center">
            <button
              v-if="canLoadMore"
              type="button"
              class="px-3 py-1.5 text-xs rounded-md border border-border/60 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors disabled:opacity-50"
              :disabled="isLoadingTrash"
              @click="loadMore"
            >
              더 보기
            </button>

            <span
              v-else-if="isLoadingMore"
              class="text-xs text-neutral-500 dark:text-neutral-400"
              >불러오는 중…</span
            >

            <span
              v-else-if="isEndReached"
              class="text-xs text-neutral-500 dark:text-neutral-400"
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
          class="mt-3 text-xs text-neutral-500 dark:text-neutral-400 text-center select-none"
        >
          {{ trash.items.length }}개 항목{{ trash.hasNext ? "+" : "" }}
        </footer>

        <!-- 자동 삭제 안내 -->
        <div class="px-6 pb-6">
          <div
            class="rounded-xl border border-border border-amber-300 bg-amber-50 px-5 py-4 text-sm"
          >
            <div class="flex items-start gap-3">
              <div class="mt-0.5 text-amber-600 text-lg">⚠️</div>
              <div>
                <p class="font-semibold text-amber-800">자동 삭제 안내</p>
                <p class="mt-1 text-amber-700 text-sm leading-relaxed">
                  휴지통에 있는 항목은 삭제 후 15일이 지나면 자동으로 영구
                  삭제됩니다. 복원하려는 항목이 있다면 15일 이내에 복원해주세요.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 삭제/비우기 확인 다이얼로그 -->
    <teleport to="#modals">
      <div
        v-if="confirmOpen"
        class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
        @click.self="closeConfirm"
        @keydown.esc="closeConfirm"
      >
        <div
          class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="dialogTitleId"
        >
          <header class="mb-4">
            <h3 :id="dialogTitleId" class="text-[17px] font-semibold leading-6">
              {{ dialogTitle }}
            </h3>
            <p class="mt-1 text-sm text-neutral-500 dark:text-neutral-400">
              {{ dialogDescription }}
            </p>
          </header>

          <footer class="mt-6 flex justify-end gap-2">
            <button
              type="button"
              class="px-4 py-2 border rounded-md text-sm hover:bg-accent"
              @click="closeConfirm"
            >
              취소
            </button>
            <button
              ref="confirmBtnRef"
              type="button"
              :disabled="confirmMutating"
              class="px-4 py-2 rounded-md text-sm bg-red-600 text-white hover:bg-red-500 disabled:opacity-50"
              @click="onConfirm"
            >
              {{ confirmActionLabel }}
            </button>
          </footer>
        </div>
      </div>
    </teleport>

    <!-- 설정 패널 -->
    <SidePanel
      :open="isSettingsOpen"
      width="min(640px, 92vw)"
      side="right"
      @close="isSettingsOpen = false"
    >
      <Settings @close="isSettingsOpen = false" />
    </SidePanel>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useToastStore } from "@/stores/toast";
import { useTrashStore } from "@/stores/trash";
import { BaseError, BaseLoading, BaseEmpty } from "@/components/ui";
import PageHeader from "@/components/layout/PageHeader.vue";
import TrashSelectionToolbar from "@/features/trash/components/TrashSelectionToolbar.vue";
import SidePanel from "@/components/overlays/SidePanel.vue";
import Settings from "@/features/settings/Settings.vue";
import { ID, TrashItem, TrashType } from "@/types/common";
import { useTrashPagingScroll } from "@/composables/useTrashPagingScroll";

const router = useRouter();

const auth = useAuthStore();
const toast = useToastStore();
const trash = useTrashStore();

const isSettingsOpen = ref(false);

// 선택 상태(로컬)
const selectedKeys = ref<Set<string>>(new Set());

const visibleKeys = computed(() => trash.items.map((i) => keyOf(i)));
const selectedCount = computed(() => selectedKeys.value.size);
const allChecked = computed(() => {
  const keys = visibleKeys.value;
  if (keys.length === 0) return false;
  return keys.every((k) => selectedKeys.value.has(k));
});

const isBulkMutating = computed(
  () => trash.isMutating.restore || trash.isMutating.delete,
);

const isLoadingTrash = computed(() => trash.isLoading);
const hasError = computed(() => !!trash.error);

function onRetry() {
  trash.safeReload();
}

function goBack() {
  if (window.history.length > 1) router.back();
  else router.push({ name: "workspace" });
}

function onOpenSettings() {
  isSettingsOpen.value = true;
}

function onOpenTagManagement() {
  isSettingsOpen.value = false;

  router.push({ name: "tags" });
}

async function onLogout() {
  try {
    await auth.logout();
    toast.info("로그아웃 되었습니다.");
    await router.replace({ path: "/login" });
  } catch (e) {
    console.error("[TrashView] 로그아웃 실패:", e);
    toast.error("로그아웃에 실패했습니다.");
  }
}

// ------------------------
// Tabs
// ------------------------
function tabClass(type: TrashType | null) {
  const active = trash.type === type;
  return active
    ? "bg-white dark:bg-zinc-900 shadow text-foreground font-medium"
    : "text-neutral-500 dark:text-neutral-400 hover:text-foreground hover:bg-white/60 dark:hover:bg-zinc-900/40";
}

function setType(type: TrashType | null) {
  isSettingsOpen.value = false;
  clearSelection();

  trash.setQuery({ type, page: 0 });
  trash.load(true).catch(() => {});
}

// ------------------------
// Selection
// ------------------------
function keyOf(item: TrashItem) {
  return `${item.type}:${item.id}`;
}

function isSelected(item: TrashItem) {
  return selectedKeys.value.has(keyOf(item));
}

function toggleSelected(item: TrashItem) {
  const key = keyOf(item);
  const next = new Set(selectedKeys.value);
  if (next.has(key)) next.delete(key);
  else next.add(key);
  selectedKeys.value = next;
}

function toggleAllVisible() {
  const keys = visibleKeys.value;
  const next = new Set(selectedKeys.value);

  const every = keys.length > 0 && keys.every((k) => next.has(k));
  if (every) {
    keys.forEach((k) => next.delete(k));
  } else {
    keys.forEach((k) => next.add(k));
  }

  selectedKeys.value = next;
}

function clearSelection() {
  selectedKeys.value = new Set();
}

// ------------------------
// Card actions
// ------------------------
function typeBarClass(type: TrashType) {
  if (type === "COLLECTION") return "bg-amber-400";
  if (type === "BOOKMARK") return "bg-sky-400";
  return "bg-emerald-400";
}

function typeLabel(type: TrashType) {
  if (type === "COLLECTION") return "컬렉션";
  if (type === "BOOKMARK") return "북마크";
  return "태그";
}

function typeBadgeClass(type: TrashType) {
  if (type === "COLLECTION")
    return "bg-amber-50 text-amber-700 borer-amber-200";
  if (type === "BOOKMARK") return "bg-sky-50 text-sky-700 border-sky-200";
  return "bg-emerald-50 text-emerald-700 border-emerald-200";
}

function fallbackEmoji(type: TrashType) {
  if (type === "COLLECTION") return "📁";
  if (type === "BOOKMARK") return "🔖";
  return "🏷️";
}

function formatRelative(iso: string) {
  const d = new Date(iso);
  const diffMs = Date.now() - d.getTime();
  const mins = Math.floor(diffMs / 60000);
  if (mins < 1) return "방금 전";
  if (mins < 60) return `${mins}분 전`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}시간 전`;
  const days = Math.floor(hours / 24);
  return `${days}일 전`;
}

function daysLeftText(iso: string) {
  const MAX_DAYS = 15;
  const d = new Date(iso);
  const diffMs = Date.now() - d.getTime();
  const daysPassed = Math.floor(diffMs / (1000 * 60 * 60 * 24));
  const left = Math.max(0, MAX_DAYS - daysPassed);
  return `${left}일 후 영구 삭제`;
}

async function onEmpty() {
  openConfirm({ kind: "EMPTY", type: trash.type });
}

async function onRestore(type: TrashType, id: ID) {
  try {
    await trash.restore(type, id);
    toast.info("복원했습니다.");

    const k = `${type}:${id}`;
    const next = new Set(selectedKeys.value);
    next.delete(k);
    selectedKeys.value = next;

    await reconnect();
  } catch {
    toast.error("복원에 실패했습니다.");
  }
}

async function onDelete(type: TrashType, id: ID) {
  const item = trash.items.find((i) => i.type === type && i.id === id);
  openConfirm({ kind: "DELETE_ONE", type, id, title: item?.title ?? null });
}

// ------------------------
// Bulk actions
// ------------------------
function selectedItems(): TrashItem[] {
  const set = selectedKeys.value;
  if (set.size === 0) return [];
  return trash.items.filter((i) => set.has(keyOf(i)));
}

function groupSelectedByType(items: TrashItem[]): Map<TrashType, ID[]> {
  const byType = new Map<TrashType, ID[]>();
  for (const it of items) {
    const arr = byType.get(it.type) ?? [];
    arr.push(it.id);
    byType.set(it.type, arr);
  }
  return byType;
}

async function onRestoreSelected() {
  const items = selectedItems();
  if (items.length === 0) return;

  const byType = groupSelectedByType(items);

  try {
    for (const [type, ids] of byType) {
      await trash.restoreBulk(type, ids);
    }
    toast.info("선택 항목을 복원했습니다.");
    clearSelection();
  } catch {
    toast.error("선택 복원에 실패했습니다.");
  }
}

async function onDeleteSelected() {
  const items = selectedItems();
  if (items.length === 0) return;
  openConfirm({ kind: "DELETE_SELECTED", items });
}

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
} = useTrashPagingScroll(listWrapRef, sentinelRef, {
  enabled: true,
  rootMargin: "150px",
  threshold: 0,
});

watch(
  () => trash.type,
  async () => {
    cleanup();
    await reconnect();
  },
);

onMounted(async () => {
  clearSelection();
  isSettingsOpen.value = false;

  trash.setQuery({ type: null, page: 0 });
  await trash.load(true).catch(() => {});

  await reconnect();
});

// ------------------------
// 삭제/비우기 확인 다이얼로그
// ------------------------
type ConfirmAction =
  | { kind: "EMPTY"; type: TrashType | null }
  | { kind: "DELETE_ONE"; type: TrashType; id: ID; title?: string | null }
  | { kind: "DELETE_SELECTED"; items: TrashItem[] };

const confirmOpen = ref(false);
const confirmAction = ref<ConfirmAction | null>(null);
const confirmBtnRef = ref<HTMLButtonElement | null>(null);

const dialogTitleId = "trash-confirm-title";

const confirmMutating = computed(() => {
  if (!confirmAction.value) return false;
  if (confirmAction.value.kind === "EMPTY") return trash.isMutating.empty;
  return trash.isMutating.delete;
});

const dialogTitle = computed(() => {
  const a = confirmAction.value;
  if (!a) return "";
  if (a.kind === "EMPTY") return "휴지통 비우기";
  if (a.kind === "DELETE_ONE") return "영구 삭제";
  return "선택 항목 영구 삭제";
});

const dialogDescription = computed(() => {
  const a = confirmAction.value;
  if (!a) return "";

  if (a.kind === "EMPTY") {
    const label = a.type ? `${typeLabel(a.type)} 휴지통` : "휴지통";
    return `${label}을(를) 비울까요? 이 작업은 되돌릴 수 없습니다.`;
  }
  if (a.kind === "DELETE_ONE") {
    const t = a.title?.trim() ? `"${a.title}"` : "이 항목";
    return `${t}을(를) 영구 삭제할까요? 이 작업은 되돌릴 수 없습니다.`;
  }
  return `선택한 ${a.items.length}개 항목을 영구 삭제할까요? 이 작업은 되돌릴 수 없습니다.`;
});

const confirmActionLabel = computed(() => {
  const a = confirmAction.value;
  if (!a) return "확인";
  if (a.kind === "EMPTY") return "비우기";
  return "삭제";
});

function openConfirm(action: ConfirmAction) {
  confirmAction.value = action;
  confirmOpen.value = true;

  nextTick(() => {
    requestAnimationFrame(() => confirmBtnRef.value?.focus());
  });
}

function closeConfirm() {
  confirmOpen.value = false;
  confirmAction.value = null;
}

async function afterMutation() {
  clearSelection();
  await reconnect();
}

async function onConfirm() {
  const action = confirmAction.value;
  if (!action) return;

  try {
    if (action.kind === "EMPTY") {
      await trash.empty(action.type);
      toast.info("휴지통을 비웠습니다.");
      closeConfirm();
      await afterMutation();
      return;
    }

    if (action.kind === "DELETE_ONE") {
      await trash.delete(action.type, action.id);
      toast.info("영구 삭제했습니다.");
      closeConfirm();
      await afterMutation();
      return;
    }

    if (action.kind === "DELETE_SELECTED") {
      const byType = groupSelectedByType(action.items);
      for (const [type, ids] of byType) await trash.deleteBulk(type, ids);
      toast.info("선택 항목을 영구 삭제했습니다.");
      closeConfirm();
      await afterMutation();
      return;
    }
  } catch {
    if (action.kind === "EMPTY") toast.error("휴지통 비우기에 실패했습니다.");
    else toast.error("영구 삭제에 실패했습니다.");
  }
}
</script>
