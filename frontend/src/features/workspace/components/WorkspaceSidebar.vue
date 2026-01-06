<template>
  <aside
    id="workspace-sidebar"
    v-bind="$attrs"
    class="relative w-64 border-r border-border flex flex-col bg-card text-card-foreground"
  >
    <!-- 상단 로고 영역 -->
    <header
      class="flex items-center justify-between px-4 h-14 border-b border-border"
    >
      <div class="flex items-center gap-2">
        <div
          class="size-7 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-md grid place-items-center"
        >
          <LogoIcon :size="16" klass="text-white" />
        </div>
        <span class="font-semibold text-sm">LinkNest</span>
      </div>
    </header>

    <!-- 컬렉션 리스트 -->
    <nav class="flex-1 min-h-0 overflow-y-auto p-2 text-sm flex flex-col">
      <div class="flex items-center justify-between px-2 py-2">
        <div class="text-muted-foreground font-medium">컬렉션</div>
        <button
          type="button"
          :disabled="isLoadingCollections || hasError || isCollectionMutating"
          class="inline-flex items-center justify-center size-7 rounded-md border border-border hover:bg-accent transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          aria-label="새 컬렉션 만들기"
          title="새 컬렉션"
          @click="onClickAddRoot"
        >
          <PlusIcon :size="16" />
        </button>
      </div>

      <!-- 상태 분기 -->
      <BaseError
        v-if="hasError"
        title="컬렉션을 불러올 수 없습니다"
        :description="collectionsError ?? undefined"
        :onRetry="onRetryCollections"
      />

      <BaseLoading
        v-else-if="isLoadingCollections"
        label="컬렉션을 불러오는 중…"
      />

      <BaseEmpty
        v-else-if="isEmpty"
        title="컬렉션이 없습니다."
        description="위 '+' 버튼으로 새 컬렉션을 추가해보세요."
      />

      <ul v-else class="space-y-1">
        <CollectionNode
          v-for="id in rootIds"
          :key="id"
          :node="collectionById[id]"
          :depth="0"
          :expanded-ids="expandedIds"
          :selected-collection-id="selectedCollectionId"
          :editing-id="editingId"
          :draft-name="draftName"
          :is-renaming="isRenaming"
          :disabled="isLoadingCollections || hasError || isCollectionMutating"
          :collection-by-id="collectionById"
          :children-by-parent="childrenByParent"
          @toggle="toggleExpand"
          @add-collection="openAddCollectionDialog"
          @open-all="$emit('open-all', $event)"
          @select-collection="handleSelectCollection"
          @update-emoji="$emit('update-emoji', $event)"
          @delete-collection="$emit('delete-collection', $event)"
          @start-rename="startRename"
          @input-rename="changeDraft"
          @submit-rename="submitRename"
          @cancel-rename="cancelRename"
          @dnd-start="onDndStart"
          @dnd-hover="onDndHover"
          @dnd-drop="onDndDrop"
        />
      </ul>

      <!-- 루트 드롭 영역(사이드바 빈 공간) -->
      <div
        :ref="setRootDropEl"
        class="flex-1 mt-1 rounded-md"
        :class="
          rootDroppable.isOvered.value
            ? 'ring-2 ring-blue-400/60 bg-blue-400/5'
            : ''
        "
        aria-label="루트로 이동 (드롭 영역)"
      />
    </nav>

    <!-- 하단 유저 메뉴 -->
    <footer class="mt-auto border-t border-border px-3 py-3">
      <UserMenu
        @open-settings="$emit('open-settings')"
        @logout="$emit('logout')"
      />
    </footer>
  </aside>

  <!-- 새 컬렉션 추가 다이얼로그 -->
  <teleport to="#modals">
    <div
      v-if="showAddCollectionDialog"
      class="fixed inset-0 z-50 bg-black/40 grid place-items-center p-4"
      @click.self="closeAddCollectionDialog"
      @keydown.esc="closeAddCollectionDialog"
    >
      <form
        class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6 relative"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="dialogTitleId"
        @submit.prevent="handleAdd"
      >
        <header class="mb-4">
          <h3 :id="dialogTitleId" class="text-[17px] font-semibold leading-6">
            새 컬렉션 추가
          </h3>
          <p class="mt-1 text-sm text-muted-foreground">
            북마크를 정리할 새로운 컬렉션을 만들어보세요.
          </p>
        </header>

        <div class="my-4 h-px bg-zinc-200/80 dark:bg-zinc-700/60"></div>

        <div class="space-y-2">
          <label class="block text-sm" :for="nameInputId">컬렉션 이름 *</label>
          <input
            :id="nameInputId"
            ref="nameInputRef"
            v-model.trim="newCollection"
            type="text"
            class="w-full rounded-md px-3 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/60"
            placeholder="컬렉션 이름"
            required
          />
        </div>

        <footer class="mt-6 flex justify-end gap-2">
          <button
            type="button"
            class="px-4 py-2 rounded-md text-sm border border-zinc-300/70 dark:border-zinc-600/60 bg-zinc-100/70 dark:bg-zinc-800/70 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            @click="closeAddCollectionDialog"
          >
            취소
          </button>
          <button
            type="submit"
            :disabled="!canSubmit || isMutating.createCollection || isAdding"
            :aria-disabled="!canSubmit"
            class="px-4 py-2 rounded-md text-sm bg-zinc-900 text-white dark:bg-zinc-100 dark:text-zinc-900 hover:bg-zinc-800 dark:hover:bg-zinc-200"
          >
            추가
          </button>
        </footer>
      </form>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ComponentPublicInstance, computed, nextTick, ref, watch } from "vue";
import { CollectionNode, UserMenu } from "@/features/workspace";
import type { ID, CollectionNode as CollectionNodeModel } from "@/types/common";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import LogoIcon from "@/components/icons/LogoIcon.vue";
import { useWorkspaceStore } from "@/stores/workspace";
import { storeToRefs } from "pinia";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";
import { useDroppable } from "@vue-dnd-kit/core";

defineOptions({ inheritAttrs: false });

const props = defineProps<{
  onAddCollection: (p: { name: string; parentId: ID | null }) => Promise<void>;
  onRenameCollection: (p: { id: ID; newName: string }) => Promise<void>;
}>();

const emit = defineEmits<{
  (e: "update-emoji", payload: { id: ID; emoji: string | null }): void;
  (e: "delete-collection", id: ID): void;
  (e: "open-all", id: ID): void;
  (e: "open-settings"): void;
  (e: "logout"): void;
}>();

const workspace = useWorkspaceStore();
const {
  collectionNodes,
  selectedCollectionId,
  isLoading,
  error,
  isMutating,
  collectionById,
  childrenByParent,
} = storeToRefs(workspace);

const isLoadingCollections = computed(() => isLoading.value.collectionTree);
const collectionsError = computed(() => error.value.collectionTree);
const hasError = computed(() => !!collectionsError.value);
const isEmpty = computed(
  () =>
    !isLoadingCollections.value &&
    !hasError.value &&
    collectionNodes.value.length === 0
);
const isCollectionMutating = computed(
  () =>
    isMutating.value.createCollection ||
    isMutating.value.updateCollection ||
    isMutating.value.updateCollectionEmoji ||
    isMutating.value.deleteCollection ||
    isMutating.value.moveCollection ||
    isMutating.value.reorderCollection
);
const rootIds = computed<ID[]>(() =>
  (childrenByParent.value["root"] ?? []).filter(
    (id) => !!collectionById.value[id]
  )
);

function handleSelectCollection(node: CollectionNodeModel) {
  workspace.selectCollection(node.id);
}

function onClickAddRoot() {
  if (isLoadingCollections.value || hasError.value) return;
  openAddCollectionDialog(null);
}

function onRetryCollections() {
  return workspace.fetchCollectionTree();
}

// 확장 상태
const expandedIds = ref<Set<ID>>(new Set());

async function toggleExpand(id: ID) {
  if (isLoadingCollections.value || hasError.value) return;

  const next = new Set(expandedIds.value);
  next.has(id) ? next.delete(id) : next.add(id);
  expandedIds.value = next;
}

// 이름 변경하기
const editingId = ref<ID | null>(null);
const draftName = ref(""); // 입력 중인 이름
const originalName = ref("");
const isRenaming = ref(false);

async function startRename(payload: { id: ID; name: string }) {
  const { id, name } = payload ?? { id: -1, name: "" };
  editingId.value = id;
  draftName.value = (name ?? "").trim();
  originalName.value = (name ?? "").trim();
  isRenaming.value = false;

  await nextTick();
  requestAnimationFrame(() => {
    const el = document.getElementById(
      `col-rename-${id}`
    ) as HTMLInputElement | null;
    el?.focus();
    el?.select();
  });
}
function changeDraft(v: string) {
  draftName.value = v ?? "";
}
function cancelRename() {
  editingId.value = null;
  draftName.value = "";
  isRenaming.value = false;
}
async function submitRename() {
  const id = editingId.value;
  const next = (draftName.value ?? "").trim();

  if (id == null) return;
  if (!next || next === originalName.value) return cancelRename();
  if (isRenaming.value) return;

  isRenaming.value = true;
  try {
    await props.onRenameCollection({ id, newName: next });
    cancelRename();
  } finally {
    isRenaming.value = false;
  }
}

// 새 컬렉션 추가 다이얼로그
const showAddCollectionDialog = ref(false);
const newCollection = ref("");
const nameInputRef = ref<HTMLInputElement | null>(null);
const dialogTitleId = "add-collection-title";
const nameInputId = "add-collection-name";
const parentIdRef = ref<ID | null>(null);

const isAdding = ref(false);
const canSubmit = computed(() => newCollection.value.trim().length > 0);

function resetAddCollectionForm() {
  newCollection.value = "";
}
function closeAddCollectionDialog() {
  resetAddCollectionForm();
  showAddCollectionDialog.value = false;
}
function openAddCollectionDialog(parentId: ID | null = null) {
  if (isLoadingCollections.value || hasError.value) return;

  resetAddCollectionForm();
  parentIdRef.value = parentId;
  showAddCollectionDialog.value = true;
}
async function handleAdd() {
  if (!canSubmit.value) return;
  if (isAdding.value) return;

  isAdding.value = true;
  try {
    await props.onAddCollection({
      name: newCollection.value.trim(),
      parentId: parentIdRef.value,
    });
    closeAddCollectionDialog();
  } finally {
    isAdding.value = false;
  }
}

// DnD
type DropZone = "top" | "middle" | "bottom";
type DndDropPayload = { targetId: ID | null; zone: DropZone };
type TemplateRefType = Element | ComponentPublicInstance | null;

const dndActiveId = ref<ID | null>(null);
const dndOver = ref<DndDropPayload | null>(null);

const rootDropEl = ref<HTMLElement | null>(null);

const rootDroppable = useDroppable({
  groups: ["collections"],
  disabled: computed(
    () =>
      isLoadingCollections.value || hasError.value || isCollectionMutating.value
  ),
  data: { type: "collection-root" },
  events: {
    onHover: () => {
      onDndHover({ targetId: null, zone: "middle" });
    },
    onLeave: () => {},
    onDrop: async () => {
      onDndDrop({ targetId: null, zone: "middle" });
      return true;
    },
  },
});

function setRootDropEl(ref: TemplateRefType) {
  const el = ref instanceof Element ? (ref as HTMLElement) : null;
  rootDroppable.elementRef.value = el;
}

function onDndStart(id: ID) {
  dndActiveId.value = id;
  dndOver.value = null;
}

function onDndHover(payload: DndDropPayload) {
  dndOver.value = payload;
}

function onDndDrop(payload: DndDropPayload) {
  const activeId = dndActiveId.value;
  dndActiveId.value = null;
  dndOver.value = null;

  if (activeId == null) return;

  const { targetId, zone } = payload;

  // 루트 드롭 -> root로 move
  if (targetId == null) {
    console.log("[DnD]", { type: "move", id: activeId, targetParentId: null });
    return;
  }

  if (activeId === targetId) return;

  const active = collectionById.value[activeId];
  const target = collectionById.value[targetId];
  if (!active || !target) return;

  const activeParentId = active.parentId ?? null;

  // 1) middle -> move
  if (zone === "middle") {
    console.log("[DnD]", {
      type: "move",
      id: activeId,
      targetParentId: targetId,
    });
    return;
  }

  // 2) top/bottom -> reorder
  const targetParentId = target.parentId ?? null;
  const key = targetParentId == null ? "root" : String(targetParentId);
  const siblings = (childrenByParent.value[key] ?? []).filter(
    (id) => !!collectionById.value[id]
  );

  const baseIndex = siblings.indexOf(targetId);
  if (baseIndex < 0) return;

  let targetIndex = zone === "top" ? baseIndex : baseIndex + 1;

  if (activeParentId === targetParentId) {
    const activeIndex = siblings.indexOf(activeId);
    if (activeIndex >= 0 && activeIndex < targetIndex) targetIndex -= 1;
  }

  console.log("[DnD]", {
    type: "reorder",
    id: activeId,
    parentId: targetParentId,
    targetIndex,
    zone,
  });
}

// 다이얼로그 포커스
watch(showAddCollectionDialog, async (open) => {
  if (open) {
    await nextTick();
    nameInputRef.value?.focus();
  }
});

watch(
  collectionNodes,
  (nodes) => {
    const alive = new Set(nodes.map((n) => n.id));
    const next = new Set([...expandedIds.value].filter((id) => alive.has(id)));

    if (next.size !== expandedIds.value.size) expandedIds.value = next;

    if (editingId.value != null && !alive.has(editingId.value)) cancelRename();
  },
  { deep: false }
);
</script>
