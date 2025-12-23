<template>
  <div class="h-screen w-full bg-background flex">
    <!-- 좌측: 사이드바 -->
    <WorkspaceSidebar
      class="w-64 border-r border-border"
      @add-collection="onAddCollection"
      @select-collection="onSelectCollection"
      @rename-collection="onRenameCollection"
      @update-emoji="onUpdateCollectionEmoji"
      @delete-collection="onDeleteCollection"
      @open-all="onOpenAllBookmarks"
      @open-settings="onOpenSettings"
      @logout="onLogout"
    />

    <!-- 우측: 메인 콘텐츠 -->
    <section class="relative flex-1 flex flex-col overflow-hidden min-h-0">
      <!-- 목록 -->
      <BookmarkList
        v-if="isListLayout"
        :key="'list-' + (selectedCollection?.id || 'none')"
        :collection="selectedCollection"
        :selected-bookmark-id="selectedBookmarkId"
        @open-add="isAddOpen = true"
        @select-bookmark="onSelectBookmark"
      />

      <BookmarkCard
        v-else
        :key="'card-' + (selectedCollection?.id || 'none')"
        :collection="selectedCollection"
        :selected-bookmark-id="selectedBookmarkId"
        @open-add="isAddOpen = true"
        @select-bookmark="onSelectBookmark"
      />

      <!-- 북마크 상세 패널 -->
      <SidePanel
        :open="selectedBookmarkId != null"
        width="min(640px, 92vw)"
        side="right"
        @after-open="editRef?.focusTitle()"
        @close="selectedBookmarkId = null"
      >
        <!-- 상태 분기 -->
        <BaseError
          v-if="hasBookmarksError"
          title="북마크를 불러올 수 없습니다"
          :description="bookmarksError ?? undefined"
          :onRetry="retryFetchBookmarks"
        />

        <BaseLoading
          v-else-if="isDetailPending"
          label="북마크 정보를 불러오는 중…"
        />

        <BaseEmpty
          v-else-if="selectedBookmark == null"
          title="북마크를 선택해주세요."
          description="북마크를 선택하면 상세 정보를 볼 수 있습니다."
        />

        <BookmarkDetail
          v-else
          ref="editRef"
          :bookmark="selectedBookmark"
          :collection-name="selectedCollection?.name"
          @close="selectedBookmarkId = null"
          @update-bookmark="onUpdateBookmark"
          @delete-bookmark="onDeleteBookmark"
          @replace-bookmark="onReplaceBookmark"
        />
      </SidePanel>

      <!-- 북마크 추가 패널 -->
      <SidePanel
        :open="isAddOpen"
        width="min(640px, 92vw)"
        side="right"
        @after-open="addRef?.focusTitle()"
        @close="isAddOpen = false"
      >
        <AddBookmarkForm
          ref="addRef"
          :open="isAddOpen"
          :collection-id="selectedCollection?.id ?? null"
          @close="isAddOpen = false"
          @submit="onAddBookmark"
        />
      </SidePanel>

      <!-- 설정 패널 -->
      <SidePanel
        :open="isSettingsOpen"
        width="min(640px, 92vw)"
        side="right"
        @close="isSettingsOpen = false"
      >
        <Settings @close="isSettingsOpen = false" />
      </SidePanel>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  WorkspaceSidebar,
  BookmarkList,
  BookmarkCard,
  BookmarkDetail,
  AddBookmarkForm,
} from "@/features/workspace";
import { Settings } from "@/features/settings";
import SidePanel from "@/components/overlays/SidePanel.vue";
import type { Bookmark, Collection, ID, ImageMode } from "@/types/common";
import { useWorkspaceStore } from "@/stores/workspace";
import * as BookmarkApi from "@/api/bookmarks";
import { storeToRefs } from "pinia";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { usePreferencesStore } from "@/stores/preferences";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";

type UpdateBookmarkPayload = {
  id: ID;
  title?: string | null;
  url: string;
  description?: string | null;
  emoji?: string | null;
};

const workspace = useWorkspaceStore();
const { collections, selectedCollectionId, bookmarks, isLoading, error } =
  storeToRefs(workspace);

const preferences = usePreferencesStore();
const { defaultLayout } = storeToRefs(preferences);

const auth = useAuthStore();
const router = useRouter();

const selectedCollection = computed<Collection | null>(() => {
  if (selectedCollectionId.value == null) return null;
  return (
    collections.value.find((c) => c.id === selectedCollectionId.value) ?? null
  );
});

const isListLayout = computed(() => defaultLayout.value === "LIST");

const selectedBookmarkId = ref<ID | null>(null);
const selectedBookmark = computed(() => {
  if (!selectedBookmarkId.value) return null;
  return bookmarks.value.find((b) => b.id === selectedBookmarkId.value) ?? null;
});

const isAddOpen = ref(false);
const isSettingsOpen = ref(false);

const addRef = ref<{ focusTitle: () => void } | null>(null);
const editRef = ref<{ focusTitle: () => void } | null>(null);

const isLoadingBookmarks = computed(() => isLoading.value.bookmarks);
const bookmarksError = computed(() => error.value.bookmarks);
const hasBookmarksError = computed(() => !!bookmarksError.value);

const isDetailOpen = computed(() => selectedBookmarkId.value != null);
const isDetailPending = computed(() => {
  // 선택했지만 아직 로딩 중이라 북마크 상세를 못 찾는 상태
  return (
    isDetailOpen.value &&
    selectedBookmark.value == null &&
    isLoadingBookmarks.value &&
    !hasBookmarksError.value
  );
});

onMounted(() => {
  workspace.fetchCollections();
});

// 핸들러
async function onSelectCollection(c: Collection) {
  workspace.selectCollection(c.id);

  selectedBookmarkId.value = null;
  isAddOpen.value = false;
  isSettingsOpen.value = false;

  await workspace.fetchBookmarks(c.id);
}

async function onAddCollection(payload: { name: string; parentId: ID | null }) {
  await workspace.createCollection({
    name: payload.name,
    parentId: payload.parentId,
    emoji: null,
  });
}

async function onRenameCollection(payload: { id: ID; newName: string }) {
  await workspace.updateCollection(payload.id, { name: payload.newName });
}

async function onUpdateCollectionEmoji(payload: {
  id: ID;
  emoji: string | null;
}) {
  await workspace.updateCollectionEmoji(payload.id, { emoji: payload.emoji });
}

async function onDeleteCollection(id: ID) {
  await workspace.deleteCollection(id);

  if (selectedCollectionId.value === id) {
    workspace.selectCollection(null);

    selectedBookmarkId.value = null;
    await workspace.fetchBookmarks();
  }
}

async function onOpenAllBookmarks(collectionId: ID) {
  try {
    let target = bookmarks.value;

    if (selectedCollection.value?.id !== collectionId) {
      target = await BookmarkApi.listBookmarks(collectionId);
    }

    const urls = target
      .map((b) => b.url?.trim())
      .filter((u): u is string => !!u);

    if (!urls.length) return;

    // 10개 초과인 경우, 확인 대화창
    if (urls.length > 10) {
      const ok = window.confirm(
        `총 ${urls.length}개의 북마크를 새 탭에서 여시겠습니까?\n` +
          "브라우저 설정에 따라 일부 탭은 차단될 수 있습니다."
      );
      if (!ok) return;
    }

    // 탭 열기 시도 + 차단된 개수 카운트
    let blockedCount = 0;
    for (const url of urls) {
      window.open(url, "_blank", "noopener,noreferrer");
    }
  } catch (e) {
    console.error("[WorkspaceView] failed to open all bookmarks", e);
  }
}

function onSelectBookmark(id: ID) {
  selectedBookmarkId.value = id;
  isSettingsOpen.value = false;
}

async function onAddBookmark(payload: {
  title?: string | null;
  url: string;
  description?: string | null;
  emoji?: string | null;
  imageMode?: ImageMode;
  collectionId: ID;
}) {
  await workspace.createBookmark({
    collectionId: payload.collectionId,
    url: payload.url,
    title: payload.title ?? null,
    description: payload.description ?? null,
    emoji: payload.emoji ?? null,
    imageMode: payload.imageMode ?? "AUTO",
  });
  await workspace.fetchBookmarks(payload.collectionId);
  isAddOpen.value = false;
}

async function onUpdateBookmark(payload: UpdateBookmarkPayload) {
  await workspace.updateBookmark(payload.id, {
    url: payload.url,
    title: payload.title,
    description: payload.description,
    emoji: payload.emoji ?? null,
  });

  if (selectedCollection.value?.id != null) {
    await workspace.fetchBookmarks(selectedCollection.value.id);
  }
}

async function onDeleteBookmark(id: ID) {
  await workspace.deleteBookmark(id);

  if (selectedCollection.value?.id != null) {
    await workspace.fetchBookmarks(selectedCollection.value.id);
  }

  selectedBookmarkId.value = null;
}

function onReplaceBookmark(updated: Bookmark) {
  workspace.replaceBookmark(updated);
}

function onOpenSettings() {
  isAddOpen.value = false;
  selectedBookmarkId.value = null;
  isSettingsOpen.value = true;
}

async function onLogout() {
  try {
    await auth.logout();
    await router.replace("/login");
  } catch (e) {
    console.error("[WorkspaceView] 로그아웃 실패:", e);
    // TODO: 에러 토스트 알림 연결
  }
}

function retryFetchBookmarks() {
  const cid = selectedCollectionId.value;
  if (cid != null) {
    workspace.fetchBookmarks(cid);
  }
}
</script>
