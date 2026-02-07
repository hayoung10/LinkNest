<template>
  <div class="h-screen w-full bg-background flex flex-col">
    <!-- 상단 헤더 -->
    <header class="h-16 border-b border-border px-6 flex items-center gap-4">
      <!-- 뒤로 -->
      <button
        type="button"
        class="inline-flex items-center gap-2 px-3 py-2 rounded-md transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800"
        @click="goBack"
      >
        <span aria-hidden="true">←</span>
        <span class="text-sm font-medium">뒤로</span>
      </button>

      <!-- 타이틀 -->
      <div class="min-w-0">
        <h1 class="text-lg font-semibold leading-6 truncate">태그 관리</h1>
        <p class="text-xs text-muted-foreground leading-4 truncate">
          북마크에 사용할 태그를 관리합니다.
        </p>
      </div>

      <div class="flex-1" />

      <!-- 우측: 유저 메뉴 -->
      <UserMenu
        :show-tag-management="false"
        @open-settings="onOpenSettings"
        @logout="onLogout"
      />
    </header>

    <!-- 본문 -->
    <main class="flex-1 min-h-0 flex">
      <!-- 좌측: 태그 사이드바 -->
      <TagSidebar
        class="w-72 border-r border-border"
        :selected-tag-id="selectedTagId"
        @select-tag="onSelectTag"
      />

      <!-- 우측: 콘텐츠 -->
      <section class="relative flex-1 flex flex-col overflow-hidden min-h-0">
        <!-- 모드 전환: dashboard / bookmarks -->
        <template v-if="mode === 'dashboard'">
          <TagDashboard
            :tag-id="selectedTagId"
            @open-bookmarks="onOpenBookmarks"
            @open-settings="onOpenSettings"
            @merge="onMerged"
            @delete="onDeleted"
          />
        </template>

        <template v-else>
          <TaggedBookmarkList
            v-if="isList"
            :key="'tagged-list-' + (selectedTagId ?? 'none')"
            :tag-id="selectedTagId"
            :selected-bookmark-id="selectedBookmarkId"
            :focus-bookmark-id="focusBookmarkId"
            @select-bookmark="onSelectBookmark"
            @back="backToDashboard"
            @open-settings="onOpenSettings"
            @tags-changed="onTagsChanged"
            @open-workspace="onOpenWorkspace"
            @focus-done="onFocusDone"
          />

          <TaggedBookmarkCard
            v-else
            :key="'tagged-card-' + (selectedTagId ?? 'none')"
            :tag-id="selectedTagId"
            :selected-bookmark-id="selectedBookmarkId"
            :focus-bookmark-id="focusBookmarkId"
            @select-bookmark="onSelectBookmark"
            @back="backToDashboard"
            @open-settings="onOpenSettings"
            @tags-changed="onTagsChanged"
            @open-workspace="onOpenWorkspace"
            @focus-done="onFocusDone"
          />
        </template>
      </section>
    </main>

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
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import SidePanel from "@/components/overlays/SidePanel.vue";
import Settings from "@/features/settings/Settings.vue";
import UserMenu from "@/features/workspace/menus/UserMenu.vue";
import {
  TagSidebar,
  TagDashboard,
  TaggedBookmarkList,
  TaggedBookmarkCard,
} from "@/features/tags";
import type { ID } from "@/types/common";
import { useAuthStore } from "@/stores/auth";
import { usePreferencesStore } from "@/stores/preferences";
import { useToastStore } from "@/stores/toast";
import { useTagsStore } from "@/stores/tags";
import { useTaggedBookmarksStore } from "@/stores/taggedBookmarks";

type Mode = "dashboard" | "bookmarks";

const router = useRouter();

const auth = useAuthStore();
const toast = useToastStore();
const preferences = usePreferencesStore();
const tagsStore = useTagsStore();
const taggedStore = useTaggedBookmarksStore();

const { defaultLayout, defaultBookmarkSort } = storeToRefs(preferences);
const isList = computed(() => defaultLayout.value === "LIST");

const selectedTagId = ref<ID | null>(null);
const mode = ref<Mode>("dashboard");
const isSettingsOpen = ref(false);

const focusBookmarkId = ref<ID | null>(null);
const selectedBookmarkId = ref<ID | null>(null);

function goBack() {
  if (window.history.length > 1) router.back();
  else router.push({ name: "workspace" });
}

function onSelectTag(id: ID) {
  selectedTagId.value = id;
  mode.value = "dashboard";
  isSettingsOpen.value = false;

  focusBookmarkId.value = null;
  selectedBookmarkId.value = null;
}

function onOpenSettings() {
  selectedBookmarkId.value = null;
  isSettingsOpen.value = true;
}

function backToDashboard() {
  mode.value = "dashboard";
  focusBookmarkId.value = null;
  selectedBookmarkId.value = null;
}

async function onOpenBookmarks(payload?: { tagId: ID; focusBookmarkId?: ID }) {
  if (payload?.tagId != null) selectedTagId.value = payload.tagId;

  mode.value = "bookmarks";
  isSettingsOpen.value = false;
  selectedBookmarkId.value = null;

  const tagId = selectedTagId.value;
  if (tagId == null) return;

  taggedStore.setContext(tagId);
  taggedStore.setQuery({ page: 0 });
  await taggedStore.load(false);

  const bookmarkId = payload?.focusBookmarkId ?? null;
  focusBookmarkId.value = bookmarkId;
  if (bookmarkId != null) {
    await ensureTaggedBookmarkVisible(bookmarkId);
  }
}

function onSelectBookmark(id: ID) {
  selectedBookmarkId.value = id;
  isSettingsOpen.value = false;
}

async function onMerged() {
  selectedTagId.value = null;
  await tagsStore.safeReload();
}

async function onDeleted() {
  selectedTagId.value = null;
  await tagsStore.safeReload();
}

async function onTagsChanged() {
  tagsStore.setQuery({ page: 0 });
  await tagsStore.safeReload();

  await taggedStore.safeReload();
}

async function refreshTagsOnEnter() {
  await tagsStore.safeReload();
}

function onOpenWorkspace(payload: { bookmarkId: ID; collectionId: ID }) {
  router.push({
    name: "workspace",
    query: {
      focusBookmarkId: String(payload.bookmarkId),
      collectionId: String(payload.collectionId),
    },
  });
}

let focusTimer: number | null = null;

function onFocusDone(id: ID) {
  if (focusTimer !== null) {
    window.clearTimeout(focusTimer);
    focusTimer = null;
  }

  // 하이라이트 유지
  focusTimer = window.setTimeout(() => {
    if (selectedBookmarkId.value != null) {
      focusBookmarkId.value = null;
      focusTimer = null;
      return;
    }

    if (focusBookmarkId.value === id) {
      focusBookmarkId.value = null;
    }

    focusTimer = null;
  }, 1200);
}

// 지정한 북마크가 목록에 보이도록 필요한 만큼 추가 페이지를 로드
async function ensureTaggedBookmarkVisible(bookmarkId: ID) {
  const MAX_PAGES = 5;
  let tries = 0;

  const hasBookmark = () => taggedStore.items.some((b) => b.id === bookmarkId);

  while (!hasBookmark()) {
    if (!taggedStore.hasNext) break;
    if (tries >= MAX_PAGES) break;

    const prevPage = taggedStore.page;

    const moved = taggedStore.nextPage();
    if (!moved) break;

    try {
      await taggedStore.load(true);
    } catch {
      taggedStore.setQuery({ page: prevPage });
      break;
    }

    tries++;
  }
}

async function refreshTaggedBookmarks() {
  if (mode.value !== "bookmarks") return;

  const tagId = selectedTagId.value;
  if (tagId == null) return;

  taggedStore.setContext(tagId);
  taggedStore.setQuery({ page: 0 });

  await taggedStore.safeReload();
}

onMounted(async () => {
  try {
    await refreshTagsOnEnter();
  } catch {
    // BaseError/토스트로 처리
  }
});

async function onLogout() {
  try {
    await auth.logout();
    toast.info("로그아웃 되었습니다.");
    await router.replace({ path: "/login" });
  } catch (e) {
    console.error("[TagManagementView] 로그아웃 실패:", e);
    toast.error("로그아웃에 실패했습니다.");
  }
}

// ------------------------
// Watchers
// ------------------------
watch(
  () => [mode.value, selectedTagId.value] as const,
  async ([m, tagId], prev) => {
    const [pm, pTagId] = prev ?? [null, null];

    if (m !== "bookmarks") return;
    if (tagId == null) return;

    if (m === pm && tagId === pTagId) return;

    selectedBookmarkId.value = null;
    isSettingsOpen.value = false;

    await refreshTaggedBookmarks();
  },
  { immediate: true },
);

watch(
  () => defaultBookmarkSort.value,
  async (sort, prevSort) => {
    if (sort === prevSort) return;

    isSettingsOpen.value = false;

    await refreshTaggedBookmarks();
  },
);
</script>
