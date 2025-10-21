<template>
  <div class="relative inline-block">
    <!-- 트리거 버튼 -->
    <button
      ref="triggerEl"
      class="inline-flex size-6 items-center justify-center rounded hover:bg-accent"
      aria-haspopup="menu"
      :aria-expanded="menuOpen"
      :aria-controls="panelId"
      @click.stop="openMenu"
    >
      <svg viewBox="0 0 24 24" class="w-4 h-4" fill="currentColor">
        <circle cx="5" cy="12" r="1.8" />
        <circle cx="12" cy="12" r="1.8" />
        <circle cx="19" cy="12" r="1.8" />
      </svg>
    </button>

    <!-- 드롭다운 메뉴 -->
    <Teleport to="#workspace-sidebar" v-if="menuOpen">
      <div
        id="collection-menu-panel"
        class="panel w-48"
        :style="panelStyle"
        role="menu"
        @click.stop
      >
        <button
          class="menu-item"
          :disabled="isOpenAllDisabled"
          :aria-disabled="isOpenAllDisabled"
          role="menuitem"
          @click="emitAndClose('open-all', collection.id)"
        >
          <svg
            class="menu-icon"
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

        <div class="divider" />

        <button
          class="menu-item"
          role="menuitem"
          @click="emitAndClose('add-sub', collection.id)"
        >
          <svg
            class="menu-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <path d="M3 7h5l2 2h11v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <path d="M12 13h6M15 10v6" />
          </svg>
          하위 컬렉션 만들기
        </button>

        <button class="menu-item" role="menuitem" @click="openRenameDialog">
          <svg
            class="menu-icon"
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

        <div class="divider" />

        <button
          class="menu-item menu-destructive"
          role="menuitem"
          @click="openDeleteDialog"
        >
          <svg
            class="menu-icon"
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
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import type { CSSProperties } from "vue";

type Collection = {
  id: string;
  name: string;
  bookmarks?: { id: string; title: string }[];
  children?: Collection[];
};

const props = defineProps<{
  collection: Collection;
}>();
const emit = defineEmits<{
  (e: "open-all", id: string): void;
  (e: "add-sub", id: string): void;
  (e: "rename", p: { collectionId: string; newName: string }): void;
  (e: "delete", id: string): void;
}>();

const menuOpen = ref(false);
const triggerEl = ref<HTMLElement | null>(null);
const showRenameDialog = ref(false);
const showDeleteDialog = ref(false);
const newName = ref(props.collection.name);

// 여러 인스턴스 안전: 패널 ID 고유화
const panelId = `collection-menu-${props.collection.id}`;

// 메뉴 비활성
const isOpenAllDisabled = computed(() => {
  const hasBookmarks = (props.collection.bookmarks?.length ?? 0) > 0;
  const hasChildren = (props.collection.children?.length ?? 0) > 0;
  return !(hasBookmarks || hasChildren);
});

// 위치 계산
const pos = ref({ top: 0, left: 0 });
const panelWidth = 192;
const gap = 8;

const panelStyle = computed<CSSProperties>(() => ({
  position: "absolute",
  top: `${pos.value.top}px`,
  left: `${pos.value.left}px`,
  width: `${panelWidth}px`,
  transform: "translateZ(0)",
  fontSize: "14px",
  lineHeight: "20px",
}));

async function openMenu() {
  menuOpen.value = true;
  nextTick(() => {
    const trigger = triggerEl.value;
    const host = document.getElementById("workspace-sidebar");
    const panel = document.getElementById("collection-menu-panel");
    if (!trigger || !host || !panel) return;

    const t = trigger.getBoundingClientRect();
    const h = host.getBoundingClientRect();

    let top = t.bottom - h.top + gap; // 트리거 아래
    let left = t.right - h.left - panelWidth; // 우측 정렬

    const ph = panel.getBoundingClientRect().height || 0;
    if (top + ph > h.height) top = t.top - h.top - ph - gap; // 아래 꽉 차면 위로

    top = Math.max(8, Math.min(top, h.height - ph - 8));
    left = Math.max(8, Math.min(left, h.width - panelWidth - 8));

    pos.value = { top: Math.round(top), left: Math.round(left) };
  });
}

function emitAndClose(e: "open-all" | "add-sub", id: string) {
  if (e === "open-all") emit("open-all", id);
  else emit("add-sub", id);
  menuOpen.value = false;
}

// 바깥 클릭 닫기
function onDocClick(e: MouseEvent) {
  const t = e.target as Node | null;
  const panel = document.getElementById("collection-menu-panel");
  if (panel?.contains(t as Node)) return;
  if (triggerEl.value?.contains(t as Node)) return;
  menuOpen.value = false;
}
onMounted(() =>
  document.addEventListener("click", onDocClick, { capture: true })
);
onBeforeUnmount(() =>
  document.removeEventListener("click", onDocClick, { capture: true })
);

// 다이얼로그 핸들러
function openRenameDialog() {
  newName.value = props.collection.name;
  showRenameDialog.value = true;
  menuOpen.value = false;
}
function openDeleteDialog() {
  showDeleteDialog.value = true;
  menuOpen.value = false;
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

<style scoped>
.panel {
  position: absolute;
  z-index: 200;
  border-radius: 12px;
  border: 1px solid color-mix(in oklab, currentColor 12%, transparent);
  background: color-mix(in oklab, var(--color-bg, #fff) 100%, transparent);
  color: var(--popover-foreground, inherit);
  box-shadow: 0 19px 30px rgba(0, 0, 0, 0.08), 0 4px 12px rgb(0, 0, 0, 0.06);
  padding: 4px;
  font-size: 14px;
  line-height: 20px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  transition: background 0.15s ease;
}

.menu-icon {
  width: 16px;
  height: 16px;
  flex: 0 0 16px;
}

.menu-item:hover {
  background: color-mix(in oklab, currentColor 10%, transparent);
}

/** 키보드 포커스 접근성 */
.menu-item:focus-visible {
  box-shadow: 0 0 0 2px
      color-mix(in oklab, var(--ring, #3b82f6) 55%, transparent),
    0 0 0 4px var(--popover, #fff);
}

.menu-item:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.menu-destructive {
  color: var(--destructive, #dc2626);
}
.menu-destructive:hover {
  background: color-mix(in oklab, #dc2626 10%, transparent);
}

.divider {
  height: 1px;
  margin: 6px 4px;
  background: color-mix(in oklab, currentColor 12%, transparent);
  border-radius: 1px;
}
</style>
