<template>
  <div class="h-screen w-full bg-background flex">
    <!-- 좌측: 사이드바 -->
    <WorkspaceSidebar
      class="w-64 border-r border-border"
      :collections="collections"
      @add-collection="onAddCollection"
      @select-collection="onSelectCollection"
    />

    <!-- 우측: 메인 콘텐츠 -->
    <section class="relative flex-1 overflow-hidden">
      <!-- 목록 -->
      <BookmarkList
        :key="selectedCollection?.id || 'none'"
        :collection="selectedCollection"
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
import { ref } from "vue";
import {
  WorkspaceSidebar,
  BookmarkList,
  BookmarkDetail,
  AddBookmarkForm,
} from "@/features/workspace";
import SidePanel from "@/components/overlays/SidePanel.vue";
import type { Bookmark, Collection } from "@/types/common";

type UpdateBookmarkPayload = {
  id: number;
  title?: string | null;
  url: string;
  description?: string | null;
};

// Mock
const now = () => new Date().toISOString();
const collections = ref<Collection[]>([
  {
    id: 1,
    name: "여행 계획",
    createdAt: now(),
    updatedAt: now(),
    children: [
      {
        id: 11,
        name: "일본 여행",
        icon: null,
        parentId: 1,
        sortOrder: 1,
        createdAt: now(),
        updatedAt: now(),
        bookmarks: [
          {
            id: 101,
            collectionId: 11,
            url: "https://example.com/osaka-food",
            title: "오사카 맛집 리스트",
            description: "현지인 추천 맛집 20곳 정리된 블로그 글이에요.",
            createdAt: now(),
            updatedAt: now(),
          },
          {
            id: 102,
            collectionId: 11,
            url: "https://example.com/kyoto-trip",
            title: "교토 여행 일정표",
            description: "3박 4일 코스로 여행 동선 정리된 글입니다.",
            createdAt: now(),
            updatedAt: now(),
          },
        ],
      },
      {
        id: 12,
        name: "유럽 여행",
        icon: null,
        parentId: 1,
        sortOrder: 2,
        createdAt: now(),
        updatedAt: now(),
        bookmarks: [
          {
            id: 103,
            collectionId: 12,
            url: "https://example.com/paris-places",
            title: "파리 관광 명소 모음",
            description: "에펠탑, 루브르, 몽마르뜨까지 주요 명소 한눈에 보기.",
            createdAt: now(),
            updatedAt: now(),
          },
        ],
      },
    ],
  },
  {
    id: 2,
    name: "라이프스타일",
    createdAt: now(),
    updatedAt: now(),
    bookmarks: [
      {
        id: 201,
        collectionId: 2,
        url: "https://example.com/daily-routine",
        title: "하루 루틴 관리 팁",
        description: "생산적인 하루를 위한 시간 관리 루틴 정리.",
        createdAt: now(),
        updatedAt: now(),
      },
      {
        id: 202,
        collectionId: 2,
        url: "https://example.com/interior-ideas",
        title: "인테리어 아이디어 모음",
        description: "작은 공간을 넓게 보이게 하는 인테리어 아이디어.",
        createdAt: now(),
        updatedAt: now(),
      },
    ],
  },
]);

const selectedCollection = ref<Collection | null>(null);
const selectedBookmark = ref<Bookmark | null>(null);
const isAddOpen = ref(false);
const addRef = ref<{ focusTitle: () => void } | null>(null);
const editRef = ref<{ focusTitle: () => void } | null>(null);

// 핸들러
function onSelectCollection(c: Collection) {
  selectedCollection.value = c;
}

function onAddCollection(name: string) {
  // TODO: createCollection API 연결
  console.log("[TODO] createCollection:", name);
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
