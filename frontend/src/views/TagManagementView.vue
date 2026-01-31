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

const { defaultLayout } = storeToRefs(preferences);
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

function onOpenBookmarks(payload?: { tagId: ID; focusBookmarkId?: ID }) {
  if (payload?.tagId != null) selectedTagId.value = payload.tagId;
  focusBookmarkId.value = payload?.focusBookmarkId ?? null;

  mode.value = "bookmarks";
  isSettingsOpen.value = false;
  selectedBookmarkId.value = null;
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

  taggedStore.safeReload();
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

watch(
  () => mode.value,
  (m) => {
    if (m === "dashboard") selectedBookmarkId.value = null;
  },
);
</script>
