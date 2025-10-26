<template>
  <div class="h-screen w-full bg-background flex">
    <!-- 좌측: 사이드바 -->
    <WorkspaceSidebar
      class="w-64 border-r border-border"
      :collections="collections"
      @add-collection="onAddCollection"
      @select-bookmark="onSelectBookmark"
    />

    <!-- 우측: 메인 콘텐츠 -->
    <section class="flex-1 overflow-auto">
      <BookmarkContent v-if="selectedBookmark" :bookmark="selectedBookmark" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { createCollection } from "@/features/workspace";
import BookmarkContent from "@/features/workspace/components/BookmarkContent.vue";
import WorkspaceSidebar from "@/features/workspace/components/WorkspaceSidebar.vue";
import { ref } from "vue";

type Bookmark = {
  id: string;
  title: string;
  description: string;
  url: string;
  collectionId: string;
};
type Collection = {
  id: string;
  name: string;
  icon?: string | null;
  children?: Collection[];
  bookmarks?: Bookmark[];
};

const collections = ref<Collection[]>([
  {
    id: "c-travel",
    name: "여행 계획",
    children: [
      {
        id: "c-japan",
        name: "일본 여행",
        bookmarks: [
          {
            id: "b1",
            title: "오사카 맛집 리스트",
            description: "현지인 추천 맛집 20곳 정리된 블로그 글이에요.",
            url: "https://example.com/osaka-food",
            collectionId: "c-japan",
          },
          {
            id: "b2",
            title: "교토 여행 일정표",
            description: "3박 4일 코스로 여행 동선 정리된 글입니다.",
            url: "https://example.com/kyoto-trip",
            collectionId: "c-japan",
          },
        ],
      },
      {
        id: "c-europe",
        name: "유럽 여행",
        bookmarks: [
          {
            id: "b3",
            title: "파리 관광 명소 모음",
            description: "에펠탑, 루브르, 몽마르뜨까지 주요 명소 한눈에 보기.",
            url: "https://example.com/paris-places",
            collectionId: "c-europe",
          },
        ],
      },
    ],
  },
  {
    id: "c-lifestyle",
    name: "라이프스타일",
    bookmarks: [
      {
        id: "b4",
        title: "하루 루틴 관리 팁",
        description: "생산적인 하루를 위한 시간 관리 루틴 정리.",
        url: "https://example.com/daily-routine",
        collectionId: "c-lifestyle",
      },
      {
        id: "b5",
        title: "인테리어 아이디어 모음",
        description: "작은 공간을 넓게 보이게 하는 인테리어 아이디어 모음.",
        url: "https://example.com/interior-ideas",
        collectionId: "c-lifestyle",
      },
    ],
  },
]);

const selectedBookmark = ref<Bookmark | null>(null);
function onSelectBookmark(b: Bookmark) {
  selectedBookmark.value = b;
}

function onAddCollection(name: string) {
  return createCollection({ name });
}
</script>
