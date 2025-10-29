<template>
  <li class="group relative">
    <!-- 컬렉션 헤더 -->
    <div
      class="relative flex items-center gap-1 px-2 py-1.5 rounded-md hover:bg-accent cursor-pointer select-none"
      role="button"
      :aria-expanded="hasChildren ? expanded : undefined"
      tabindex="0"
      :style="{ paddingLeft: `${depth * 12}px` }"
      @click.stop="$emit('select-collection', node)"
      @keydown.enter.stop="$emit('select-collection', node)"
      @keydown.space.prevent.stop="
        hasChildren
          ? $emit('toggle', node.id)
          : $emit('select-collection', node)
      "
      @keydown.arrow-right.prevent.stop="
        hasChildren && !expanded && $emit('toggle', node.id)
      "
      @keydown.arrow-left.prevent.stop="
        hasChildren && expanded && $emit('toggle', node.id)
      "
    >
      <!-- 토글 아이콘 -->
      <button
        v-if="hasChildren"
        type="button"
        class="size-5 grid place-items-center text-muted-foreground rounded hover:bg-accent/50"
        aria-label="하위 항목 토글"
        @click.stop="$emit('toggle', node.id)"
      >
        <svg
          class="size-4 transition-transform"
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
      </button>
      <span v-else class="inline-block w-5" aria-hidden="true"></span>

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
      <span class="truncate flex-1" :title="node.name">{{ node.name }}</span>

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
          @select-collection="$emit('select-collection', $event)"
        />
      </ul>
    </div>
  </li>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Collection } from "@/types/common";
import CollectionMenu from "../menus/CollectionMenu.vue";

defineOptions({ name: "CollectionNode" });

type CountMode = "direct" | "aggregate";

const props = withDefaults(
  defineProps<{
    node: Collection;
    depth?: number;
    expandedIds: Set<number>;
    countMode?: CountMode;
  }>(),
  { depth: 0, countMode: "aggregate" as CountMode }
);
const emit = defineEmits<{
  (e: "toggle", id: number): void;
  (e: "select-collection", c: Collection): void;
}>();

const expanded = computed(() => props.expandedIds.has(props.node.id));
const hasChildren = computed(() => (props.node.children?.length ?? 0) > 0);

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
