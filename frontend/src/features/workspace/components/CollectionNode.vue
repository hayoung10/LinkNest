<template>
  <li class="group relative">
    <!-- 컬렉션 헤더 -->
    <div
      :class="[
        'relative flex items-center gap-1 px-2 py-1.5 rounded-md cursor-pointer select-none',
        isActive
          ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-100 font-medium ring-1 ring-blue-300'
          : 'hover:bg-zinc-100 dark:hover:bg-zinc-800',
      ]"
      role="button"
      :aria-expanded="hasChildren ? expanded : undefined"
      :tabindex="isEditing ? -1 : 0"
      :style="{ paddingLeft: `${depth * 12}px` }"
      v-on="nodeHandlers"
    >
      <!-- 토글 아이콘 -->
      <button
        v-if="hasChildren"
        type="button"
        class="size-5 grid place-items-center text-muted-foreground rounded hover:bg-accent/50"
        aria-label="하위 항목 토글"
        @click.stop="$emit('toggle', node.id)"
      >
        <ChevronIcon
          :size="16"
          :direction="expanded ? 'down' : 'right'"
          class="size-4 transition-transform duration-150"
        />
      </button>
      <span v-else class="inline-block w-5" aria-hidden="true"></span>

      <!-- 폴더 아이콘 -->
      <FolderIcon :size="16" class="size-4 text-muted-foreground opacity-80" />

      <!-- 이름 -->
      <div class="flex-1 min-w-0">
        <span v-if="!isEditing" class="truncate" :title="node.name">{{
          node.name
        }}</span>
        <input
          v-else
          :id="`col-rename-${node.id}`"
          type="text"
          class="w-full rounded-sm px-2 py-1 text-sm bg-background border border-border focus:outline-none focus:ring-2 focus:ring-ring/50"
          :value="draftName"
          :aria-label="`컬렉션 이름 편집: ${node.name}`"
          @click.stop
          @mousedown.stop
          @input="
            $emit('input-rename', ($event.target as HTMLInputElement).value)
          "
          @keydown.enter.stop="$emit('submit-rename')"
          @keydown.esc.stop="$emit('cancel-rename')"
          @blur="$emit('submit-rename')"
        />
      </div>

      <!-- 북마크 개수 -->
      <span
        v-if="bookmarkCount && !isEditing"
        class="text-xs text-muted-foreground tabular-nums"
        >{{ bookmarkCount }}</span
      >

      <!-- … 메뉴 버튼 -->
      <div
        v-if="!isEditing"
        class="ml-1 opacity-0 group-hover:opacity-100 transition-opacity"
        @click.stop
      >
        <CollectionMenu
          :collection="node"
          :editing-id="editingId"
          :draft-name="draftName"
          :is-renaming="isRenaming"
          @open-all="() => {}"
          @add-sub="() => {}"
          @start-rename="$emit('start-rename', $event)"
          @delete="$emit('delete-collection', node.id)"
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
          :selected-id="selectedCollectionId"
          :count-mode="countMode"
          :editing-id="editingId"
          :draft-name="draftName"
          :is-renaming="isRenaming"
          @toggle="$emit('toggle', $event)"
          @select-collection="$emit('select-collection', $event)"
          @delete-collection="$emit('delete-collection', $event)"
          @start-rename="$emit('start-rename', $event)"
          @input-rename="$emit('input-rename', $event)"
          @submit-rename="$emit('submit-rename')"
          @cancel-rename="$emit('cancel-rename')"
        />
      </ul>
    </div>
  </li>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { CollectionMenu } from "@/features/workspace";
import type { Collection, ID } from "@/types/common";
import ChevronIcon from "@/components/icons/ChevronIcon.vue";
import FolderIcon from "@/components/icons/FolderIcon.vue";

defineOptions({ name: "CollectionNode" });

type CountMode = "direct" | "aggregate";

const props = withDefaults(
  defineProps<{
    node: Collection;
    depth?: number;
    expandedIds: Set<number>;
    countMode?: CountMode;
    selectedCollectionId?: number | null;

    // 이름 변경에 대한 상태
    editingId?: number | null;
    draftName?: string;
    isRenaming?: boolean;
  }>(),
  {
    depth: 0,
    countMode: "aggregate" as CountMode,
    selectedCollectionId: null,
    editingId: null,
    draftName: "",
    isRenaming: false,
  }
);

const emit = defineEmits<{
  (e: "toggle", id: number): void;
  (e: "select-collection", c: Collection): void;
  (e: "delete-collection", id: ID): void;

  // 이름 변경 이벤트
  (e: "start-rename", payload: { id: ID; name: string }): void;
  (e: "input-rename", value: string): void;
  (e: "submit-rename"): void;
  (e: "cancel-rename"): void;
}>();

const expanded = computed(() => props.expandedIds.has(props.node.id));
const hasChildren = computed(() => (props.node.childCount ?? 0) > 0);
const bookmarkCount = computed(() => props.node.bookmarkCount ?? 0);
const isEditing = computed(() => props.editingId === props.node.id);
const isActive = computed(() => props.selectedCollectionId === props.node.id);

// 핸들러 (편집 중일 때 비활성화)
const nodeHandlers = computed(() => {
  if (isEditing.value) return {};

  return {
    click: (e: MouseEvent) => {
      e.stopPropagation();
      emit("select-collection", props.node);
    },
    keydown: (e: KeyboardEvent) => {
      if (e.key === "Enter") {
        e.stopPropagation();
        emit("select-collection", props.node);
      } else if (e.key === " ") {
        e.preventDefault();
        e.stopPropagation();
        hasChildren.value
          ? emit("toggle", props.node.id)
          : emit("select-collection", props.node);
      } else if (e.key === "ArrowRight") {
        e.preventDefault();
        e.stopPropagation();
        if (hasChildren.value && !expanded.value) emit("toggle", props.node.id);
      } else if (e.key === "ArrowLeft") {
        e.preventDefault();
        e.stopPropagation();
        if (hasChildren.value && expanded.value) emit("toggle", props.node.id);
      }
    },
  };
});
</script>
