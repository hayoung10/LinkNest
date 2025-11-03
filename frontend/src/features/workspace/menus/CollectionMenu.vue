<template>
  <div class="relative inline-block">
    <!-- 트리거 버튼 -->
    <button
      ref="triggerEl"
      class="inline-flex size-6 items-center justify-center rounded hover:bg-accent"
      aria-haspopup="menu"
      :aria-expanded="menuOpen"
      :aria-controls="panelId"
      @click.stop="toggleMenu"
    >
      <svg viewBox="0 0 24 24" class="w-4 h-4" fill="currentColor">
        <circle cx="5" cy="12" r="1.8" />
        <circle cx="12" cy="12" r="1.8" />
        <circle cx="19" cy="12" r="1.8" />
      </svg>
    </button>

    <!-- 드롭다운 메뉴 -->
    <teleport to="#modals" v-if="menuOpen">
      <div
        :id="panelId"
        class="panel w-48"
        :style="panelStyle"
        role="menu"
        aria-orientation="vertical"
        @click.stop
        @keydown.esc.prevent.stop="closeMenu"
      >
        <button
          class="menu-item"
          :disabled="isOpenAllDisabled"
          :aria-disabled="isOpenAllDisabled"
          role="menuitem"
          ref="firstItemRef"
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

        <button class="menu-item" role="menuitem" @click="startRename">
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
          이름 변경하기
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
    </teleport>

    <!-- 삭제 확인 다이얼로그 -->
    <teleport to="#modals">
      <div
        v-if="showDeleteDialog"
        class="fixed inset-0 z-[130] bg-black/40 grid place-items-center p-4"
        @click.self="showDeleteDialog = false"
        @keydown.esc="showDeleteDialog = false"
      >
        <div
          class="w-full max-w-md rounded-2xl border border-zinc-200/70 dark:border-zinc-700/60 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 shadow-[0_10px_40px_rgba(0,0,0,.12)] backdrop-blur-sm p-6 relative"
        >
          <header class="mb-4">
            <h3 class="text-[17px] font-semibold leading-6">컬렉션 삭제</h3>
            <p class="mt-1 text-sm text-muted-foreground">
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
              class="px-4 py-2 rounded-md text-sm bg-red-600 text-white hover:bg-red-500"
              @click="handleDelete"
            >
              삭제
            </button>
          </footer>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
  type CSSProperties,
} from "vue";
import type { Collection, ID } from "@/types/common";

const props = defineProps<{
  collection: Collection;
}>();
const emit = defineEmits<{
  (e: "open-all", id: ID): void;
  (e: "add-sub", id: ID): void;
  (e: "start-rename", payload: { id: ID; name: string }): void;
  (e: "delete", id: ID): void;
}>();

const menuOpen = ref(false);
const triggerEl = ref<HTMLElement | null>(null);
const firstItemRef = ref<HTMLElement | null>(null);

const ignoreNextDocClick = ref(false);
const panelId = `collection-menu-${props.collection.id}`;

const showDeleteDialog = ref(false);

// 메뉴 비활성
const isOpenAllDisabled = computed(() => {
  const hasBookmarks = (props.collection.bookmarks?.length ?? 0) > 0;
  const hasChildren = (props.collection.children?.length ?? 0) > 0;
  return !(hasBookmarks || hasChildren);
});

// 위치 계산 (뷰포트 기준: position fixed)
const pos = ref({ top: 0, left: 0 });
const panelWidth = 192;
const gap = 8;

const panelStyle = computed<CSSProperties>(() => ({
  position: "fixed",
  top: `${pos.value.top}px`,
  left: `${pos.value.left}px`,
  width: `${panelWidth}px`,
  zIndex: 60,
  transform: "translateZ(0)",
}));

function updatePosition() {
  const t = triggerEl.value?.getBoundingClientRect();
  const panel = document.getElementById(panelId);
  if (!t || !panel) return;

  const ph = panel.offsetHeight || 0;
  const vw = window.innerWidth;
  const vh = window.innerHeight;

  // 트리거 아래, 우측 정렬
  let top = t.bottom + gap;
  let left = t.right - panelWidth;

  if (top + ph > vh - 8) top = Math.max(8, t.top - ph - gap); // 아래 꽉 차면 위로
  left = Math.min(Math.max(8, left), vw - panelWidth - 8);

  pos.value = { top: Math.round(top), left: Math.round(left) };
}

function toggleMenu() {
  menuOpen.value ? closeMenu() : openMenu();
}
function closeMenu() {
  if (!menuOpen.value) return;
  menuOpen.value = false;
  triggerEl.value?.focus();
}
async function openMenu() {
  if (menuOpen.value) return;
  ignoreNextDocClick.value = true;
  menuOpen.value = true;

  await nextTick();
  await new Promise(requestAnimationFrame);

  updatePosition();
  firstItemRef.value?.focus();
  requestAnimationFrame(() => {
    ignoreNextDocClick.value = false;
  });
}

// 바깥 클릭 닫기
function onDocClick(e: MouseEvent) {
  if (!menuOpen.value || ignoreNextDocClick.value) return;
  const t = e.target as Node | null;
  const panel = document.getElementById(panelId);
  if (panel?.contains(t) || triggerEl.value?.contains(t)) return;
  closeMenu();
}

function onScrollOrResize() {
  if (!menuOpen.value) return;
  updatePosition();
}

onMounted(() => {
  document.addEventListener("click", onDocClick, { capture: true });
  window.addEventListener("resize", onScrollOrResize, { passive: true });
  window.addEventListener("scroll", onScrollOrResize, { passive: true });
});
onBeforeUnmount(() => {
  document.removeEventListener("click", onDocClick, { capture: true });
  window.removeEventListener("resize", onScrollOrResize);
  window.removeEventListener("scroll", onScrollOrResize);
});

function emitAndClose(e: "open-all" | "add-sub", id: ID) {
  if (e === "open-all") emit("open-all", id);
  else emit("add-sub", id);
  closeMenu();
}

function startRename() {
  emit("start-rename", {
    id: props.collection.id,
    name: props.collection.name,
  });
  closeMenu();
}

// 다이얼로그 핸들러
function openDeleteDialog() {
  showDeleteDialog.value = true;
  closeMenu();
}
function handleDelete() {
  emit("delete", props.collection.id);
  showDeleteDialog.value = false;
}
</script>

<style scoped>
.panel {
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

/** 키보드 포커스 */
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
