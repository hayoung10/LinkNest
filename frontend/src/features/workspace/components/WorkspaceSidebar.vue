<template>
  <aside
    id="workspace-sidebar"
    class="relative isolate overflow-visible w-64 border-r border-border flex flex-col bg-card text-card-foreground"
  >
    <!-- 상단 로고 영역 -->
    <header
      class="flex items-center justify-between px-4 h-14 border-b border-border"
    >
      <div class="flex items-center gap-2">
        <div
          class="size-7 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-md grid place-items-center"
        >
          <svg
            class="size-4 text-white"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <path d="M10 13a5 5 0 0 1 7 0l2 2a5 5 0 0 1-7 7l-2-2" />
            <path d="M14 11a5 5 0 0 1-7 0l-2-2a5 5 0 1 1 7-7l2 2" />
          </svg>
        </div>
        <span class="font-semibold text-sm">LinkNest</span>
      </div>
    </header>

    <!-- 컬렉션 리스트 -->
    <nav class="flex-1 overflow-y-auto p-2 text-sm relative">
      <div class="px-2 py-2 text-muted-foreground font-medium">컬렉션</div>
      <ul class="space-y-1">
        <CollectionNode
          v-for="c in collections"
          :key="c.id"
          :node="c"
          :depth="0"
          @select-bookmark="(b: Bookmark) => $emit('select-bookmark', b)"
        />
      </ul>

      <!-- 컬렉션 추가 버튼 -->
      <button
        class="mt-2 w-full text-left px-2 py-1.5 rounded-md border border-dashed text-muted-foreground hover:bg-accent transition-colors"
      >
        + 컬렉션 추가
      </button>
    </nav>

    <!-- 하단 유저 메뉴 -->
    <footer class="mt-auto border-t border-border px-3 py-3 relative">
      <UserMenu />
    </footer>
  </aside>
</template>

<script setup lang="ts">
import UserMenu from "../menus/UserMenu.vue";
import CollectionNode from "./CollectionNode.vue";

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

defineProps<{ collections: Collection[] }>();
defineEmits<{ (e: "select-bookmark", b: Bookmark): void }>();
</script>
