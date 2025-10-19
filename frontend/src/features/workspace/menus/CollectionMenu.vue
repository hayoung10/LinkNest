<template>
  <div class="relative inline-block">
    <!-- 트리거 버튼 -->
    <button
      ref="triggerEl"
      class="inline-flex items-center justify-center w-6 h-6 rounded hover:bg-accent transition-colors"
      @click.stop="openMenu"
      aria-haspopup="menu"
      :aria-expanded="menuOpen"
      title="컬렉션 메뉴"
    >
      <svg viewBox="0 0 24 24" class="size-4" fill="currentColor">
        <circle cx="5" cy="12" r="1.8" />
        <circle cx="12" cy="12" r="1.8" />
        <circle cx="19" cy="12" r="1.8" />
      </svg>
    </button>

    <!-- 드롭다운 메뉴 -->
    <Teleport to="body">
      <div
        v-if="menuOpen"
        id="collection-menu-panel"
        ref="panelEl"
        class="fixed z-[120] rounded-md border bg-popover text-popover-foreground shadow-lg text-sm"
        :style="{ top: pos.top + 'px', left: pos.left + 'px', width: '192px' }"
        @mousedown.stop
        @click.stop
      >
        <button
          class="w-full flex items-center gap-2 px-3 py-2 hover:bg-accent disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="isOpenAllDisabled"
          @click="emitAndCloseOpenAll"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <path
              d="M18 13v6a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"
            />
            <path d="M15 3h6v6" />
            <path d="M10 14L21 3" />
          </svg>
          모든 북마크 열기
        </button>

        <div class="my-1 h-px bg-border" />

        <button
          class="w-full flex items-center gap-2 px-3 py-2 hover:bg-accent"
          @click="emitAndCloseAddSub"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <path d="M3 7h5l2 2h11v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <path d="M12 13h6M15 10v6" />
          </svg>
          하위 컬렉션 만들기
        </button>

        <button
          class="w-full flex items-center gap-2 px-3 py-2 hover:bg-accent"
          @click="openRenameDialog"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <path d="M12 20h9" />
            <path
              d="M16.5 3.5a2.121 2.121 0 1 1 3 3L7 19l-4 1 1-4 12.5-12.5z"
            />
          </svg>
          이름 변경
        </button>

        <div class="my-1 h-px bg-border" />

        <button
          class="w-full flex items-center gap-2 px-3 py-2 text-destructive hover:bg-accent/70"
          @click="openDeleteDialog"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <path d="M3 6h18" />
            <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
            <path d="M10 11v6M14 11v6" />
          </svg>
          삭제
        </button>
      </div>
    </Teleport>

    <!-- 이름 변경 다이얼로그 -->
    <div
      v-if="showRenameDialog"
      class="fixed inset-0 z-[130] bg-black/50 grid place-items-center p-4"
      @click.self="showRenameDialog = false"
    >
      <div
        class="bg-card text-card-foreground w-full max-w-sm rounded-xl shadow-xl p-6"
      >
        <header class="mb-4">
          <h3 class="text-lg font-semibold">컬렉션 이름 변경</h3>
          <p class="text-sm text-muted-foreground">
            새로운 컬렉션 이름을 입력해주세요.
          </p>
        </header>
        <div class="space-y-2">
          <label class="block text-sm">컬렉션 이름</label>
          <input
            v-model="newName"
            type="text"
            class="w-full border rounded-md px-3 py-2 text-sm bg-background"
            placeholder="컬렉션 이름"
            autofocus
          />
        </div>
        <footer class="mt-6 flex justify-end gap-2">
          <button
            class="px-4 py-2 border rounded-md text-sm hover:bg-accent"
            @click="showRenameDialog = false"
          >
            취소
          </button>
          <button
            class="px-4 py-2 rounded-md bg-primary text-white hover:bg-primary/90"
            @click="handleRename"
          >
            변경
          </button>
        </footer>
      </div>
    </div>

    <!-- 삭제 확인 다이얼로그 -->
    <div
      v-if="showDeleteDialog"
      class="fixed inset-0 z-[130] bg-black/50 grid place-items-center p-4"
      @click.self="showDeleteDialog = false"
    >
      <div
        class="bg-card text-card-foreground w-full max-w-sm rounded-xl shadow-xl p-6"
      >
        <header class="mb-4">
          <h3 class="text-lg font-semibold">컬렉션 삭제</h3>
          <p class="text-sm text-muted-foreground">
            정말로 "{{ collection.name }}" 컬렉션을 삭제하시겠습니까?<br />
            이 컬렉션에 포함된 모든 북마크도 함께 삭제됩니다. 이 작업은 되돌릴
            수 없습니다.
          </p>
        </header>
        <footer class="mt-6 flex justify-end gap-2">
          <button
            class="px-4 py-2 border rounded-md text-sm hover:bg-accent"
            @click="showDeleteDialog = false"
          >
            취소
          </button>
          <button
            class="px-4 py-2 rounded-md bg-destructive text-white hover:bg-destructive/90"
            @click="handleDelete"
          >
            삭제
          </button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
} from "vue";

type Bookmark = { id: string; title: string };
type Collection = {
  id: string;
  name: string;
  bookmarks?: Bookmark[];
  children?: Collection[];
};

const props = defineProps<{
  collection: Collection;
}>();
const emit = defineEmits<{
  (e: "open-all", collectionId: string): void;
  (e: "add-sub", parentId: string): void;
  (e: "rename", payload: { collectionId: string; newName: string }): void;
  (e: "delete", collectionId: string): void;
}>();

// 메뉴 열림 상태 & 위치
const menuOpen = ref(false);
const triggerEl = ref<HTMLElement | null>(null);
const panelEl = ref<HTMLDivElement | null>(null);
const pos = reactive({ top: 0, left: 0 });

// 다이얼로그 상태
const showRenameDialog = ref(false);
const showDeleteDialog = ref(false);
const newName = ref(props.collection.name);

// 비활성 조건
const isOpenAllDisabled = computed(() => {
  const hasBookmarks = (props.collection.bookmarks?.length ?? 0) > 0;
  const hasChildren = (props.collection.children?.length ?? 0) > 0;
  return !(hasBookmarks || hasChildren);
});

async function openMenu() {
  menuOpen.value = true;
  await nextTick();
  updatePosition();
}
function closeMenu() {
  menuOpen.value = false;
}

function updatePosition() {
  const trg = triggerEl.value;
  const panel = panelEl.value;
  if (!trg || !panel) return;

  const r = trg.getBoundingClientRect();
  let w = panel.getBoundingClientRect().width || 192;
  if (w < 120 || w > 400) w = 192;

  pos.left = r.right - w + window.scrollX; // 우측 정렬
  pos.top = r.bottom + 6 + window.scrollY;
}

// 바깥 클릭 닫기
function onDocClick(e: MouseEvent) {
  const t = e.target as Node | null;
  const panel = document.getElementById("collection-menu-panel");
  if (panel?.contains(t as Node)) return;
  if (triggerEl.value?.contains(t as Node)) return;
  closeMenu();
}
onMounted(() => document.addEventListener("click", onDocClick));
onBeforeUnmount(() => document.removeEventListener("click", onDocClick));

function emitAndCloseOpenAll() {
  emit("open-all", props.collection.id);
  closeMenu();
}
function emitAndCloseAddSub() {
  emit("add-sub", props.collection.id);
  closeMenu();
}

// 다이얼로그 핸들러
function openRenameDialog() {
  newName.value = props.collection.name;
  showRenameDialog.value = true;
  closeMenu();
}
function openDeleteDialog() {
  showDeleteDialog.value = true;
  closeMenu();
}
function handleRename() {
  const name = (newName.value ?? "").trim();
  if (name && name !== props.collection.name) {
    emit("rename", { collectionId: props.collection.id, newName: name });
  }
  showRenameDialog.value = false;
}
function handleDelete() {
  emit("delete", props.collection.id);
  showDeleteDialog.value = false;
}
</script>
