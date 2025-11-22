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
    <nav class="flex-1 overflow-y-auto p-2 text-sm">
      <div class="flex items-center justify-between px-2 py-2">
        <div class="text-muted-foreground font-medium">컬렉션</div>
        <button
          type="button"
          class="inline-flex items-center justify-center size-7 rounded-md border border-border hover:bg-accent transition-colors"
          aria-label="새 컬렉션 만들기"
          title="새 컬렉션"
          @click="openAddCollectionDialog"
        >
          <PlusIcon :size="16" />
        </button>
      </div>

      <ul class="space-y-1">
        <CollectionNode
          v-for="c in collections"
          :key="c.id"
          :node="c"
          :depth="0"
          :expanded-ids="expandedIds"
          :editing-id="editingId"
          :draft-name="draftName"
          :is-renaming="isRenaming"
          @toggle="toggleExpand"
          @select-collection="$emit('select-collection', $event)"
          @delete-collection="$emit('delete-collection', $event)"
          @start-rename="startRename"
          @input-rename="changeDraft"
          @submit-rename="submitRename"
          @cancel-rename="cancelRename"
        />
      </ul>
    </nav>

    <!-- 하단 유저 메뉴 -->
    <footer class="mt-auto border-t border-border px-3 py-3">
      <UserMenu />
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
            :disabled="!canSubmit"
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
import { computed, nextTick, ref, watch } from "vue";
import { CollectionNode, UserMenu } from "@/features/workspace";
import type { Collection, ID } from "@/types/common";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import LogoIcon from "@/components/icons/LogoIcon.vue";
import { useWorkspaceStore } from "@/stores/workspace";

defineOptions({ inheritAttrs: false });

const props = defineProps<{ collections: Collection[] }>();
const emit = defineEmits<{
  (e: "select-collection", c: Collection): void;
  (e: "add-collection", name: string): void;
  (e: "rename-collection", p: { id: number; newName: string }): void;
  (e: "delete-collection", id: ID): void;
}>();

const workspace = useWorkspaceStore();

// 확장 상태
const expandedIds = ref<Set<number>>(new Set());

async function toggleExpand(id: number) {
  const next = new Set(expandedIds.value);

  if (!next.has(id)) {
    // 펼쳐야 하는 경우
    await workspace.fetchChildCollections(id);
    next.add(id);
  } else {
    next.delete(id);
  }

  expandedIds.value = next;
}

// 이름 변경하기
const editingId = ref<number | null>(null);
const draftName = ref(""); // 입력 중인 이름
const originalName = ref("");
const isRenaming = ref(false);

async function startRename(payload: { id: number; name: string }) {
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
function submitRename() {
  const id = editingId.value;
  const next = (draftName.value ?? "").trim();

  if (id == null) return;
  if (!next || next === originalName.value) {
    cancelRename();
    return;
  }

  isRenaming.value = true;
  emit("rename-collection", { id, newName: next });
  isRenaming.value = false;
  cancelRename();
}

// 새 컬렉션 추가 다이얼로그
const showAddCollectionDialog = ref(false);
const newCollection = ref("");
const nameInputRef = ref<HTMLInputElement | null>(null);
const dialogTitleId = "add-collection-title";
const nameInputId = "add-collection-name";

const canSubmit = computed(() => newCollection.value.trim().length > 0);

function resetAddCollectionForm() {
  newCollection.value = "";
}
function closeAddCollectionDialog() {
  resetAddCollectionForm();
  showAddCollectionDialog.value = false;
}
function openAddCollectionDialog() {
  resetAddCollectionForm();
  showAddCollectionDialog.value = true;
}
function handleAdd() {
  if (!canSubmit.value) return;
  emit("add-collection", newCollection.value.trim());
  closeAddCollectionDialog();
}

// 다이얼로그 포커스
watch(showAddCollectionDialog, async (open) => {
  if (open) {
    await nextTick();
    nameInputRef.value?.focus();
  }
});
</script>
