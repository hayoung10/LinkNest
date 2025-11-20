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
        :key="currentCollection?.id || 'none'"
        :collection="currentCollection"
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
          :collection-name="currentCollection?.name"
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
          :collection-id="currentCollection?.id ?? null"
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
const { collections, currentCollection, selectedCollectionId } =
  storeToRefs(workspace);

const selectedBookmark = ref<Bookmark | null>(null);
const isAddOpen = ref(false);
const addRef = ref<{ focusTitle: () => void } | null>(null);
const editRef = ref<{ focusTitle: () => void } | null>(null);

onMounted(() => {
  workspace.fetchCollections();
});

// 핸들러
function onSelectCollection(c: Collection) {
  workspace.selectCollection(c.id);
}

async function onAddCollection(name: string) {
  await workspace.createCollection({ name, parentId: null, icon: null });
}

async function onRenameCollection(payload: { id: ID; newName: string }) {
  await workspace.updateCollection(payload.id, { name: payload.newName });
}

async function onDeleteCollection(id: ID) {
  await workspace.deleteCollection(id);
}

function onSelectBookmark(b: Bookmark) {
  selectedBookmark.value = b;
}

function onAddBookmark(payload: {
  title?: string | null;
  url: string;
  description?: string | null;
  collectionId: number;
}) {
  // TODO: createBookmark API 연결
  console.log("[TODO] createBookmark:", payload);
  isAddOpen.value = false;
}

function onUpdateBookmark(payload: UpdateBookmarkPayload) {
  // TODO: updateBookmark API 연결
  console.log("[TODO] updateBookmark:", payload);
}

function onDeleteBookmark(id: number) {
  // TODO: deleteBookmark API 연결
  console.log("[TODO] deleteBookmark id:", id);
  selectedBookmark.value = null;
}
</script>
