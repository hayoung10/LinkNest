<template>
  <div class="h-screen w-full bg-background flex">
    <!-- 좌측: 사이드바 -->
    <WorkspaceSidebar
      class="w-64 border-r border-border"
      :collections="collections"
      @add-collection="onAddCollection"
      @select-collection="onSelectCollection"
      @rename-collection="onRenameCollection"
      @delete-collection="onDeleteCollection"
      @open-all="onOpenAllBookmarks"
      @open-settings="onOpenSettings"
      @logout="onLogout"
    />

    <!-- 우측: 메인 콘텐츠 -->
    <section class="relative flex-1 flex flex-col overflow-hidden min-h-0">
      <!-- 목록 -->
      <BookmarkList
        :key="selectedCollection?.id || 'none'"
        :collection="selectedCollection"
        :bookmarks="bookmarks"
        :selected-bookmark-id="selectedBookmark?.id ?? null"
        @open-add="isAddOpen = true"
        @select-bookmark="onSelectBookmark"
      />

      <!-- 북마크 상세 패널 -->
      <SidePanel
        :open="!!selectedBookmark"
        width="min(640px, 92vw)"
        side="right"
        @after-open="editRef?.focusTitle()"
        @close="selectedBookmark = null"
      >
        <BookmarkDetail
          v-if="selectedBookmark"
          ref="editRef"
          :bookmark="selectedBookmark"
          :collection-name="selectedCollection?.name"
          @close="selectedBookmark = null"
          @update-bookmark="onUpdateBookmark"
          @delete-bookmark="onDeleteBookmark"
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
  BookmarkDetail,
  AddBookmarkForm,
} from "@/features/workspace";
import { Settings } from "@/features/settings";
import SidePanel from "@/components/overlays/SidePanel.vue";
import type { Bookmark, Collection, ID } from "@/types/common";
import { useWorkspaceStore } from "@/stores/workspace";
import * as BookmarkApi from "@/api/bookmarks";
import { storeToRefs } from "pinia";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";

type UpdateBookmarkPayload = {
  id: ID;
  title?: string | null;
  url: string;
  description?: string | null;
};

const workspace = useWorkspaceStore();
const { collections, selectedCollectionId, bookmarks } = storeToRefs(workspace);

const auth = useAuthStore();
const router = useRouter();

const selectedCollection = computed<Collection | null>(() => {
  if (selectedCollectionId.value == null) return null;
  return (
    collections.value.find((c) => c.id === selectedCollectionId.value) ?? null
  );
});

const selectedBookmark = ref<Bookmark | null>(null);

const isAddOpen = ref(false);
const isSettingsOpen = ref(false);

const addRef = ref<{ focusTitle: () => void } | null>(null);
const editRef = ref<{ focusTitle: () => void } | null>(null);

onMounted(() => {
  workspace.fetchCollections();
});

// 핸들러
async function onSelectCollection(c: Collection) {
  workspace.selectCollection(c.id);

  selectedBookmark.value = null;
  isAddOpen.value = false;
  isSettingsOpen.value = false;

  await workspace.fetchBookmarks(c.id);
}

async function onAddCollection(payload: { name: string; parentId: ID | null }) {
  await workspace.createCollection({
    name: payload.name,
    parentId: payload.parentId,
    icon: null,
  });
}

async function onRenameCollection(payload: { id: ID; newName: string }) {
  await workspace.updateCollection(payload.id, { name: payload.newName });
}

async function onDeleteCollection(id: ID) {
  await workspace.deleteCollection(id);

  if (selectedCollectionId.value === id) {
    workspace.selectCollection(null);

    selectedBookmark.value = null;
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

function onSelectBookmark(b: Bookmark) {
  selectedBookmark.value = b;
  isSettingsOpen.value = false;
}

async function onAddBookmark(payload: {
  title?: string | null;
  url: string;
  description?: string | null;
  collectionId: ID;
}) {
  await workspace.createBookmark({
    collectionId: payload.collectionId,
    url: payload.url,
    title: payload.title ?? undefined,
    description: payload.description ?? undefined,
  });
  await workspace.fetchBookmarks(payload.collectionId);
  isAddOpen.value = false;
}

async function onUpdateBookmark(payload: UpdateBookmarkPayload) {
  await workspace.updateBookmark(payload.id, {
    url: payload.url,
    title: payload.title,
    description: payload.description,
  });

  // 패널 업데이트
  const updated = workspace.bookmarks.find((b) => b.id === payload.id);
  if (updated) {
    selectedBookmark.value = updated;
  }

  if (selectedCollection.value?.id != null) {
    await workspace.fetchBookmarks(selectedCollection.value.id);
  }
}

async function onDeleteBookmark(id: ID) {
  await workspace.deleteBookmark(id);

  if (selectedCollection.value?.id != null) {
    await workspace.fetchBookmarks(selectedCollection.value.id);
  }

  selectedBookmark.value = null;
}

function onOpenSettings() {
  isAddOpen.value = false;
  selectedBookmark.value = null;
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
</script>
