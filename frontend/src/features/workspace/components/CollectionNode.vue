<template>
  <li class="group relative" :ref="setNodeEl">
    <!-- 컬렉션 헤더 -->
    <div
      :ref="setDroppableEl"
      :class="[
        'relative w-full max-w-full flex items-center gap-0.5 pl-2 pr-3 py-1 rounded-md cursor-pointer select-none',
        isActive
          ? 'bg-blue-50 text-blue-900 dark:bg-blue-900/20 dark:text-blue-100 font-medium'
          : !props.dndActiveId
            ? 'hover:bg-zinc-100 dark:hover:bg-zinc-800'
            : '',

        draggable.isDragging.value ? 'opacity-60 scale-[0.99]' : '',
      ]"
      role="button"
      :aria-expanded="hasChildren ? expanded : undefined"
      :tabindex="isEditing || disabled || isRenaming ? -1 : 0"
      :style="{ paddingLeft: `${8 + depth * 12}px` }"
      v-on="nodeHandlers"
    >
      <div
        v-if="isActive"
        class="absolute left-0 top-1 bottom-1 w-[3px] rounded-r bg-blue-500"
      />

      <span class="inline-flex w-5 justify-center shrink-0">
        <button
          v-show="!isEditing && !disabled && !isRenaming"
          :ref="setDraggableEl"
          type="button"
          class="size-5 grid place-items-center rounded text-muted-foreground opacity-55 hover:opacity-100 hover:bg-accent/50 cursor-grab active:cursor-grabbing transition"
          style="touch-action: none"
          aria-label="드래그로 이동"
          title="드래그"
          @pointerdown.stop.prevent="
            (e) => {
              $emit('dnd-start', node.id);
              draggable.handleDragStart(e);
            }
          "
          @click.stop
          @mousedown.stop
        >
          <GripVerticalIcon :size="12" />
        </button>
      </span>

      <!-- 토글 아이콘 -->
      <button
        v-if="hasChildren"
        type="button"
        :disabled="disabled || isEditing || isRenaming"
        class="size-5 grid place-items-center text-muted-foreground rounded hover:bg-accent/50 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-transparent"
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

      <!-- 아이콘 (폴더 / 이모지) -->
      <span class="inline-flex size-5 items-center justify-center ml-0.5">
        <span
          v-if="node.emoji"
          class="text-base leading-none"
          aria-hidden="true"
          >{{ node.emoji }}</span
        >
        <FolderIcon
          v-else
          :size="16"
          class="size-4 text-muted-foreground opacity-80"
        />
      </span>

      <!-- 이름 -->
      <div class="flex-1 min-w-0">
        <span
          v-if="!isEditing"
          class="block w-full truncate"
          :title="node.name"
          >{{ node.name }}</span
        >
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
      <div v-if="!isEditing" class="ml-auto shrink-0 flex items-center gap-1">
        <span
          v-if="bookmarkCount"
          class="shrink-0 text-[11px] tabular-nums px-2 py-0.5 rounded-full bg-zinc-100/90 text-zinc-700 dark:bg-zinc-800/90 dark:text-zinc-200"
          >{{ bookmarkCount }}</span
        >

        <!-- … 메뉴 버튼 -->
        <div
          v-if="!isEditing"
          class="w-8 flex justify-center opacity-0 group-hover:opacity-100 transition-opacity"
          @click.stop
        >
          <CollectionMenu
            :collection="node"
            :editing-id="editingId"
            :draft-name="draftName"
            :is-renaming="isRenaming"
            :disabled="disabled || isRenaming"
            @open-all="$emit('open-all', $event)"
            @add-collection="$emit('add-collection', $event)"
            @start-rename="$emit('start-rename', $event)"
            @update-emoji="$emit('update-emoji', $event)"
            @delete="$emit('delete-collection', node.id)"
          />
        </div>
      </div>

      <!-- zone 하이라이트 오버레이 -->
      <div v-if="isDropTarget" class="pointer-events-none absolute inset-0">
        <!-- middle -->
        <div
          v-if="overZone === 'middle'"
          class="absolute inset-0 rounded-md ring-2 ring-indigo-500/70 bg-transparent"
        />
        <!-- top -->
        <div
          v-else-if="overZone === 'top'"
          class="absolute h-[2px] rounded bg-indigo-500"
          :style="[reorderLineStyle, { top: '1px' }]"
        />
        <!-- bottom -->
        <div
          v-else
          class="absolute h-[2px] rounded bg-indigo-500"
          :style="[reorderLineStyle, { bottom: '1px' }]"
        />
      </div>
    </div>

    <!-- 내용(하위 컬렉션 + 북마크) -->
    <div v-if="expanded">
      <!-- 재귀: 하위 컬렉션 -->
      <ul v-if="childIds.length" class="mt-1 space-y-1">
        <CollectionNode
          v-for="childId in childIds"
          :key="childId"
          :node="collectionById[childId]"
          :depth="depth + 1"
          :expanded-set="expandedSet"
          :selected-collection-id="selectedCollectionId"
          :editing-id="editingId"
          :draft-name="draftName"
          :is-renaming="isRenaming"
          :disabled="disabled"
          :collection-by-id="collectionById"
          :children-by-parent="childrenByParent"
          :dnd-active-id="dndActiveId"
          :dnd-over="dndOver"
          @toggle="$emit('toggle', $event)"
          @add-collection="$emit('add-collection', $event)"
          @open-all="$emit('open-all', $event)"
          @select-collection="$emit('select-collection', $event)"
          @update-emoji="$emit('update-emoji', $event)"
          @delete-collection="$emit('delete-collection', $event)"
          @start-rename="$emit('start-rename', $event)"
          @input-rename="$emit('input-rename', $event)"
          @submit-rename="$emit('submit-rename')"
          @cancel-rename="$emit('cancel-rename')"
          @dnd-start="$emit('dnd-start', $event)"
          @dnd-hover="$emit('dnd-hover', $event)"
          @dnd-drop="$emit('dnd-drop', $event)"
        />
      </ul>
    </div>
  </li>
</template>

<script setup lang="ts">
import {
  ComponentPublicInstance,
  computed,
  onBeforeUnmount,
  ref,
  watchEffect,
} from "vue";
import { useDnDStore, useDraggable, useDroppable } from "@vue-dnd-kit/core";
import { CollectionMenu } from "@/features/workspace";
import type { ID, CollectionNode as CollectionNodeModel } from "@/types/common";
import ChevronIcon from "@/components/icons/ChevronIcon.vue";
import FolderIcon from "@/components/icons/FolderIcon.vue";
import GripVerticalIcon from "@/components/icons/GripVerticalIcon.vue";

type DropZone = "top" | "middle" | "bottom";
type DndDropPayload = { targetId: ID | null; zone: DropZone };

defineOptions({ name: "CollectionNode" });

const dndStore = useDnDStore();

const props = withDefaults(
  defineProps<{
    node: CollectionNodeModel;
    depth?: number;
    expandedSet: Set<ID>;
    selectedCollectionId?: ID | null;
    disabled?: boolean;

    collectionById: Record<ID, CollectionNodeModel>;
    childrenByParent: Record<string, ID[]>;

    // 이름 변경에 대한 상태
    editingId?: ID | null;
    draftName?: string;
    isRenaming?: boolean;

    dndActiveId?: ID | null;
    dndOver?: DndDropPayload | null;
  }>(),
  {
    depth: 0,
    selectedCollectionId: null,
    disabled: false,
    editingId: null,
    draftName: "",
    isRenaming: false,
    dndActiveId: null,
    dndOver: null,
  },
);

const emit = defineEmits<{
  (e: "toggle", id: ID): void;
  (e: "add-collection", parentId: ID): void;
  (e: "open-all", id: ID): void;
  (e: "select-collection", c: CollectionNodeModel): void;
  (e: "delete-collection", id: ID): void;
  (e: "update-emoji", payload: { id: ID; emoji: string | null }): void;

  // 이름 변경 이벤트
  (e: "start-rename", payload: { id: ID; name: string }): void;
  (e: "input-rename", value: string): void;
  (e: "submit-rename"): void;
  (e: "cancel-rename"): void;

  (e: "dnd-start", id: ID): void;
  (e: "dnd-hover", payload: DndDropPayload): void;
  (e: "dnd-drop", payload: DndDropPayload): void;
}>();

const expanded = computed(() => props.expandedSet.has(props.node.id));
const isEditing = computed(() => props.editingId === props.node.id);
const isActive = computed(() => props.selectedCollectionId === props.node.id);

const childIds = computed<ID[]>(() => {
  const key = String(props.node.id);
  const ids = props.childrenByParent[key] ?? [];
  return ids.filter((id) => props.collectionById[id] != null);
});

const hasChildren = computed(() => childIds.value.length > 0);
const bookmarkCount = computed(() => props.node.bookmarkCount ?? 0);

const isDropTarget = computed(() => {
  if (!props.dndActiveId) return false;
  if (!props.dndOver) return false;
  if (props.dndOver.targetId !== props.node.id) return false;
  if (props.dndActiveId === props.node.id) return false;
  return true;
});
const overZone = computed<DropZone>(() => props.dndOver?.zone ?? "middle");

const disableDnd = computed(
  () => props.disabled || isEditing.value || !!props.isRenaming,
);

const droppable = useDroppable({
  groups: ["collections"],
  disabled: disableDnd,
  data: {
    type: "collection",
    id: props.node.id,
    parentId: props.node.parentId ?? null,
  },
  events: {
    onDrop: async () => {
      emit("dnd-drop", { targetId: props.node.id, zone: currentZone.value });
      return true;
    },
  },
});

const draggable = useDraggable({
  id: props.node.id,
  groups: ["collections"],
  disabled: disableDnd,
  data: {
    type: "collection",
    id: props.node.id,
    parentId: props.node.parentId ?? null,
  },
});

type TemplateRefType = Element | ComponentPublicInstance | null;

const nodeEl = ref<HTMLElement | null>(null);
const scrollParentEl = ref<HTMLElement | null>(null);

function setNodeEl(ref: TemplateRefType) {
  const el = (ref instanceof HTMLElement ? ref : null) as HTMLElement | null;
  nodeEl.value = el;

  if (el) {
    const scroller = el.closest(
      "[data-collection-scroll]",
    ) as HTMLElement | null;
    scrollParentEl.value = scroller;
  }
}

function setDroppableEl(ref: TemplateRefType) {
  const el = ref instanceof Element ? (ref as HTMLElement) : null;
  droppable.elementRef.value = el;
}

function setDraggableEl(ref: TemplateRefType) {
  const el = ref instanceof Element ? (ref as HTMLElement) : null;
  draggable.elementRef.value = el;
}

function getDropZone(pointerY: number, el: HTMLElement): DropZone {
  const rect = el.getBoundingClientRect();
  const y = pointerY - rect.top;
  const h = Math.max(1, rect.height);

  const ratio = y / h; // 0~1
  if (ratio <= 0.25) return "top";
  if (ratio >= 0.75) return "bottom";
  return "middle";
}

const currentZone = computed<DropZone>(() => {
  const el = droppable.elementRef.value;
  const pointerY = dndStore.pointerPosition?.current?.value?.y;

  if (!el || pointerY == null) return "middle";
  return getDropZone(pointerY, el);
});

const reorderLineStyle = computed(() => {
  const left = props.depth * 12;
  return {
    left: `${left}px`,
    right: "8px",
    width: "auto",
    position: "absolute",
  } as const;
});

const lastHover = ref<string>(""); // 직전 hover 상태

// auto scroll
let rafId: number | null = null;

function autoScrollOnce(pointerY: number) {
  const scroller = scrollParentEl.value;
  if (!scroller) return;

  const rect = scroller.getBoundingClientRect();
  const threshold = 44;
  const maxStep = 18;

  const distTop = pointerY - rect.top;
  const distBottom = rect.bottom - pointerY;

  let delta = 0;

  if (distTop >= 0 && distTop < threshold) {
    const t = 1 - distTop / threshold; // 0~1
    delta = -1 * Math.ceil(maxStep * t);
  } else if (distBottom >= 0 && distBottom < threshold) {
    const t = 1 - distBottom / threshold;
    delta = Math.ceil(maxStep * t);
  }

  if (delta !== 0) {
    scroller.scrollTop += delta;
  }
}

function scheduleAutoScroll(pointerY: number) {
  if (rafId != null) return;
  rafId = requestAnimationFrame(() => {
    rafId = null;
    autoScrollOnce(pointerY);
  });
}

onBeforeUnmount(() => {
  if (rafId != null) cancelAnimationFrame(rafId);
});

// 핸들러 (편집 중일 때 비활성화)
const nodeHandlers = computed(() => {
  if (props.disabled || isEditing.value || props.isRenaming) return {};

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

// dnd-hover + auto scroll 트리거
watchEffect(() => {
  if (!props.dndActiveId) {
    lastHover.value = "";
    return;
  }

  const pointerY =
    dndStore.pointerPosition?.current?.value?.y ??
    (dndStore.pointerPosition as any)?.value?.y;

  if (pointerY != null) scheduleAutoScroll(pointerY);

  if (!droppable.isOvered.value) return;

  if (props.dndActiveId === props.node.id) return;

  const payload: DndDropPayload = {
    targetId: props.node.id,
    zone: currentZone.value,
  };
  const key = `${payload.targetId}:${payload.zone}`;

  if (key !== lastHover.value) {
    lastHover.value = key;
    emit("dnd-hover", payload);
  }
});
</script>
