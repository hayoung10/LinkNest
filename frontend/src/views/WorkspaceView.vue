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
    />

    <!-- 우측: 메인 콘텐츠 -->
    <section class="relative flex-1 overflow-hidden">
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
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import {
  WorkspaceSidebar,
  BookmarkList,
  BookmarkDetail,
  AddBookmarkForm,
} from "@/features/workspace";
import SidePanel from "@/components/overlays/SidePanel.vue";
import type { Bookmark, Collection, ID } from "@/types/common";
import { useWorkspaceStore } from "@/stores/workspace";
import { storeToRefs } from "pinia";

type UpdateBookmarkPayload = {
  id: number;
  title?: string | null;
  url: string;
  description?: string | null;
};

const workspace = useWorkspaceStore();
const { collections, selectedCollectionId, bookmarks } = storeToRefs(workspace);

const selectedCollection = ref<Collection | null>(null);
const selectedBookmark = ref<Bookmark | null>(null);

const isAddOpen = ref(false);
const addRef = ref<{ focusTitle: () => void } | null>(null);
const editRef = ref<{ focusTitle: () => void } | null>(null);

onMounted(() => {
  workspace.fetchCollections();
});

// 핸들러
async function onSelectCollection(c: Collection) {
  selectedCollection.value = c;
  workspace.selectCollection(c.id);

  selectedBookmark.value = null;
  isAddOpen.value = false;

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
    selectedCollection.value = null;
    selectedBookmark.value = null;
    await workspace.fetchBookmarks();
  }
}

function onSelectBookmark(b: Bookmark) {
  selectedBookmark.value = b;
}

async function onAddBookmark(payload: {
  title?: string | null;
  url: string;
  description?: string | null;
  collectionId: number;
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

async function onDeleteBookmark(id: number) {
  await workspace.deleteBookmark(id);

  if (selectedCollection.value?.id != null) {
    await workspace.fetchBookmarks(selectedCollection.value.id);
  }

  selectedBookmark.value = null;
}
</script>
