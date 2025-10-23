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
    </div>
  </li>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
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
  { depth: 0, expanded: false, countMode: "aggregate" }
);
const emit = defineEmits<{
  (e: "toggle", id: string): void;
  (e: "select-bookmark", b: Bookmark): void;
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
</script>
