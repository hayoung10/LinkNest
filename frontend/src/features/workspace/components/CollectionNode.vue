<template>
  <li class="group relative">
    <!-- 컬렉션 헤더 -->
    <div
      class="relative flex items-center gap-1 px-2 py-1.5 rounded-md hover:bg-accent cursor-pointer select-none"
      role="button"
      :aria-expanded="expanded"
      tabindex="0"
      :style="{ paddingLeft: `${depth * 12}px` }"
      @click.stop="$emit('toggle', node.id)"
      @keydown.enter.stop="$emit('toggle', node.id)"
      @keydown.space.prevent.stop="$emit('toggle', node.id)"
    >
      <!-- 토글 아이콘 -->
      <svg
        class="size-4 text-muted-foreground transition-transform"
        :class="expanded ? 'rotate-90' : ''"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M9 18l6-6-6-6" />
      </svg>

      <!-- 폴더 아이콘 -->
      <svg
        class="size-4 text-muted-foreground opacity-80"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M3 7h5l2 2h11v9" />
        <path d="M3 7v10a2 2 0 0 0 2 2h16" />
      </svg>

      <!-- 이름 -->
      <span class="truncate flex-1">{{ node.name }}</span>

      <!-- 북마크 개수 -->
      <span
        v-if="totalCount"
        class="text-xs text-muted-foreground tabular-nums"
        >{{ totalCount }}</span
      >

      <!-- … 메뉴 버튼 -->
      <div
        class="ml-1 opacity-0 group-hover:opacity-100 transition-opacity"
        @click.stop
      >
        <CollectionMenu
          :collection="node"
          @open-all="() => {}"
          @add-sub="() => {}"
          @rename="() => {}"
          @delete="() => {}"
          @add-bookmark="() => (showAddBookmarkDialog = true)"
        />
      </div>
    </div>

    <!-- 내용(하위 컬렉션 + 북마크) -->
    <div v-if="expanded">
      <!-- 재귀: 하위 컬렉션 -->
      <ul v-if="node.children?.length" class="mt-1 space-y-1">
        <CollectionNode
          v-for="child in node.children || []"
          :key="child.id"
          :node="child"
          :depth="depth + 1"
          :expanded-ids="expandedIds"
          @toggle="$emit('toggle', $event)"
          @select-bookmark="$emit('select-bookmark', $event)"
        />
      </ul>

      <!-- 북마크 리스트 -->
      <ul v-if="node.bookmarks?.length">
        <li
          v-for="b in node.bookmarks"
          :key="b.id"
          class="pl-4 pr-2 py-1.5 rounded-md flex items-center gap-2 hover:bg-accent cursor-pointer select-none"
          :style="{ paddingLeft: `${depth * 12 + 24}px` }"
          role="button"
          :title="b.title"
          @click.stop="$emit('select-bookmark', b)"
        >
          <svg
            class="size-4 text-muted-foreground opacity-80"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M14 2H6a2 2 0 0 0-2 2v16l6-3 6 3V8z" />
          </svg>
          <span class="truncate">{{ b.title }}</span>
        </li>
      </ul>

      <!-- 북마크 추가 버튼 -->
      <li
        class="pl-4 pr-2 py-1.5 rounded-md flex items-center gap-2 text-muted-foreground hover:text-foreground hover:bg-accent cursor-pointer select-none"
        :style="{ paddingLeft: `${depth * 12 + 24}px` }"
        role="button"
        tabindex="0"
        @click.stop="openAddBookmark"
        @keydown.enter.stop="openAddBookmark"
        @keydown.space.prevent.stop="openAddBookmark"
      >
        <span class="text-base leading-none">+</span>
        <span class="text-sm">북마크 추가</span>
      </li>
    </div>
  </li>

  <!-- 새 북마크 추가 다이얼로그 -->
  <div
    v-if="showAddBookmarkDialog"
    class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
    @click.self="closeDialog"
    @keydown.esc="closeDialog"
  >
    <div
      class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6 relative"
    >
      <header class="mb-4">
        <h3 class="text-[17px] font-semibold leading-6">새 북마크 추가</h3>
        <p class="mt-1 text-sm text-muted-foreground">
          저장하고 싶은 링크의 정보를 입력해주세요.
        </p>
      </header>
      <div class="my-4 h-px bg-zinc-200/80 dark:bg-zinc-700/60"></div>
      <div class="space-y-2">
        <label class="block text-sm">제목 *</label>
        <input
          v-model="newBookmark.title"
          type="text"
          class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
          placeholder="북마크 제목"
          autofocus
        />
        <label class="block text-sm">링크 *</label>
        <input
          v-model="newBookmark.url"
          type="text"
          class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
          placeholder="http://example.com"
          autofocus
        />
        <label class="block text-sm">설명</label>
        <input
          v-model="newBookmark.description"
          type="text"
          class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
          placeholder="북마크에 대한 설명을 입력하세요."
          autofocus
        />
      </div>
      <footer class="mt-6 flex justify-end gap-2">
        <button
          class="px-4 py-2 rounded-md text-sm border border-zinc-300/70 dark:border-zinc-600/60 bg-zinc-100/70 dark:bg-zinc-800/70 hover:bg-zinc-100 dark:hover:bg-zinc-800"
          @click="closeDialog"
        >
          취소
        </button>
        <button
          class="px-4 py-2 rounded-md text-sm bg-zinc-900 text-white dark:bg-zinc-100 dark:text-zinc-900 hover:bg-zinc-800 dark:hover:bg-zinc-200"
          :disabled="!canSubmit"
          @click="handleAdd"
        >
          변경
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import CollectionMenu from "../menus/CollectionMenu.vue";

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

defineOptions({ name: "CollectionNode" });

const props = withDefaults(
  defineProps<{
    node: Collection;
    depth?: number;
    expandedIds: Set<string>;
    countMode?: "direct" | "aggregate";
  }>(),
  { depth: 0, countMode: "aggregate" }
);
const emit = defineEmits<{
  (e: "toggle", id: string): void;
  (e: "select-bookmark", b: Bookmark): void;
  (e: "add-bookmark", b: Omit<Bookmark, "id">): void;
}>();

const expanded = computed(() => props.expandedIds.has(props.node.id));

// 총합 카운트
function countAll(n: Collection): number {
  const self = n.bookmarks?.length ?? 0;
  const child = (n.children ?? []).reduce((acc, c) => acc + countAll(c), 0);
  return self + child;
}
const directCount = computed(() => props.node.bookmarks?.length ?? 0);
const totalCount = computed(() =>
  props.countMode === "aggregate" ? countAll(props.node) : directCount.value
);

const showAddBookmarkDialog = ref(false);
const newBookmark = reactive({
  title: "",
  url: "",
  description: "",
});

const canSubmit = computed(() => newBookmark.url.trim().length > 0);

function openAddBookmark() {
  showAddBookmarkDialog.value = true;
}
function resetForm() {
  newBookmark.title = "";
  newBookmark.url = "";
  newBookmark.description = "";
}
function closeDialog() {
  showAddBookmarkDialog.value = false;
  resetForm();
}
function handleAdd() {
  if (!canSubmit.value) return;
  emit("add-bookmark", {
    title: newBookmark.title.trim(),
    url: newBookmark.url.trim(),
    description: newBookmark.description.trim(),
    collectionId: props.node.id,
  });
  closeDialog();
}
</script>
