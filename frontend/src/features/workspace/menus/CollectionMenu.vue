<template>
  <div class="relative inline-block">
    <!-- 트리거 버튼 -->
    <button
      ref="triggerEl"
      type="button"
      :disabled="isDisabled"
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
        @keydown.esc.prevent.stop="closeMenu()"
      >
        <button
          class="menu-item"
          role="menuitem"
          ref="firstItemRef"
          :disabled="isDisabled"
          @click="emitAndClose('open-all', collection.id)"
        >
          <ExternalLinkIcon class="menu-icon" :stroke-width="1" />
          모든 북마크 열기
        </button>

        <div class="divider" />

        <!-- Emoji 섹션 -->
        <button
          ref="emojiTriggerEl"
          class="menu-item"
          role="menuitem"
          :disabled="isDisabled"
          @click.stop="openEmojiPicker"
        >
          <span
            class="menu-icon grid place-items-center text-base leading-none"
          >
            {{ collection.emoji ?? "😊" }}
          </span>
          {{ collection.emoji ? "이모지 변경하기" : "이모지 추가하기" }}
        </button>

        <button
          v-if="collection.emoji"
          class="menu-item"
          role="menuitem"
          :disabled="isDisabled"
          @click="removeEmoji"
        >
          <span class="menu-icon grid place-items-center text-base leading-none"
            >✖️</span
          >
          이모지 제거하기
        </button>

        <div class="divider" />

        <button
          class="menu-item"
          role="menuitem"
          :disabled="isDisabled"
          @click="emitAndClose('add-collection', collection.id)"
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

        <button
          class="menu-item"
          role="menuitem"
          :disabled="isDisabled"
          @click="startRename"
        >
          <EditIcon class="menu-icon" :stroke-width="1" />
          이름 변경하기
        </button>

        <div class="divider" />

        <button
          class="menu-item menu-destructive"
          role="menuitem"
          :disabled="isDisabled"
          @click="openDeleteDialog"
        >
          <TrashIcon class="menu-icon" :stroke-width="1" />
          삭제
        </button>
      </div>
    </teleport>

    <!-- 이모지 picker popover -->
    <teleport to="#modals">
      <div
        v-if="emojiPickerOpen"
        class="fixed inset-0 z-[200]"
        tabindex="-1"
        @keydown.esc.prevent.stop="closeEmojiPicker(true)"
      >
        <div class="absolute inset-0" @click="closeEmojiPicker(false)" />
        <div
          :style="emojiPickerStyle"
          class="absolute w-[360px] rounded-xl border border-border bg-card shadow-lg p-2"
          @click.stop
        >
          <EmojiPicker @select="onEmojiSelected" />
        </div>
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
              "{{ collection.name }}" 컬렉션을 휴지통으로 이동하시겠습니까?<br />
              이 컬렉션 아래의 모든 하위 컬렉션과 북마크도 함께 이동됩니다.
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
              :disabled="isDisabled"
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
  ref,
  watch,
  type CSSProperties,
} from "vue";
import type { ID, CollectionNode as CollectionNodeModel } from "@/types/common";
import ExternalLinkIcon from "@/components/icons/ExternalLinkIcon.vue";
import TrashIcon from "@/components/icons/TrashIcon.vue";
import EditIcon from "@/components/icons/EditIcon.vue";
import EmojiPicker from "vue3-emoji-picker";
import "vue3-emoji-picker/css";
import { storeToRefs } from "pinia";
import { useWorkspaceStore } from "@/stores/workspace";

const props = defineProps<{
  collection: CollectionNodeModel;
  disabled?: boolean;
}>();
const emit = defineEmits<{
  (e: "open-all", id: ID): void;
  (e: "add-collection", id: ID): void;
  (e: "start-rename", payload: { id: ID; name: string }): void;
  (e: "delete", id: ID): void;
  (e: "update-emoji", payload: { id: ID; emoji: string | null }): void;
}>();

const { isMutating } = storeToRefs(useWorkspaceStore());

const isMenuMutating = computed(() => {
  const m = isMutating.value;
  return (
    m.createCollection ||
    m.updateCollection ||
    m.updateCollectionEmoji ||
    m.deleteCollection
  );
});
const isDisabled = computed(() => !!props.disabled || isMenuMutating.value);

// 상태
const menuOpen = ref(false);
const triggerEl = ref<HTMLElement | null>(null);
const firstItemRef = ref<HTMLElement | null>(null);

const panelId = `collection-menu-${props.collection.id}`;
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

const showDeleteDialog = ref(false);

const emojiPickerOpen = ref(false);
const emojiTriggerEl = ref<HTMLElement | null>(null);
const emojiPos = ref({ top: 0, left: 0 });
const emojiPickerWidth = 360;
const emojiGap = 8;

const emojiPickerStyle = computed<CSSProperties>(() => ({
  position: "fixed",
  top: `${emojiPos.value.top}px`,
  left: `${emojiPos.value.left}px`,
  zIndex: 70,
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

function openMenu() {
  if (isDisabled.value) return;
  if (menuOpen.value) return;
  menuOpen.value = true;

  nextTick(() => {
    updatePosition();
    requestAnimationFrame(() => firstItemRef.value?.focus());
  });

  document.addEventListener("click", onDocClick, { capture: true });
}
function closeMenu(returnFocus = true) {
  if (!menuOpen.value) return;
  menuOpen.value = false;
  document.removeEventListener("click", onDocClick, { capture: true });

  if (returnFocus) requestAnimationFrame(() => triggerEl.value?.focus());
}
function toggleMenu() {
  if (isDisabled.value) return;
  menuOpen.value ? closeMenu() : openMenu();
}

// 바깥 클릭 닫기
function onDocClick(e: MouseEvent) {
  if (!menuOpen.value) return;
  const t = e.target as Node | null;
  const panel = document.getElementById(panelId);
  if (panel?.contains(t) || triggerEl.value?.contains(t)) return;
  closeMenu();
}

// emoji picker 함수
function updateEmojiPickerPosition() {
  const t = emojiTriggerEl.value?.getBoundingClientRect();
  if (!t) return;

  const left = Math.min(
    Math.max(t.left, 8),
    window.innerWidth - emojiPickerWidth - 8,
  );

  emojiPos.value = {
    top: Math.round(t.bottom + emojiGap),
    left: Math.round(left),
  };
}
function onEmojiViewportChange(_e: Event) {
  if (!emojiPickerOpen.value) return;
  updateEmojiPickerPosition();
}
function openEmojiPicker() {
  if (isDisabled.value) return;
  closeMenu(false);
  emojiPickerOpen.value = true;

  nextTick(() => updateEmojiPickerPosition());

  window.addEventListener("scroll", onEmojiViewportChange, true);
  window.addEventListener("resize", onEmojiViewportChange);
}
function closeEmojiPicker(returnFocus = false) {
  if (!emojiPickerOpen.value) return;
  emojiPickerOpen.value = false;

  window.removeEventListener("scroll", onEmojiViewportChange, true);
  window.removeEventListener("resize", onEmojiViewportChange);

  if (returnFocus) {
    requestAnimationFrame(() => emojiTriggerEl.value?.focus());
  }
}
function removeEmoji() {
  if (isDisabled.value) return;
  emit("update-emoji", { id: props.collection.id, emoji: null });
  closeMenu(false);
}
function onEmojiSelected(emoji: any) {
  if (isDisabled.value) return;
  const picked = emoji?.i ?? null;
  if (!picked) return;

  emit("update-emoji", { id: props.collection.id, emoji: picked });
  closeEmojiPicker(false);
  closeMenu(false);
}

onBeforeUnmount(() => {
  document.removeEventListener("click", onDocClick, { capture: true });
  window.removeEventListener("scroll", onEmojiViewportChange, true);
  window.removeEventListener("resize", onEmojiViewportChange);
});

function emitAndClose(e: "open-all" | "add-collection", id: ID) {
  if (e === "open-all") emit("open-all", id);
  else emit("add-collection", id);
  closeMenu();
}

function startRename() {
  emit("start-rename", {
    id: props.collection.id,
    name: props.collection.name,
  });
  closeMenu(false);
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

watch(isDisabled, (w) => {
  if (w) {
    closeEmojiPicker(false);
    closeMenu(false);
    showDeleteDialog.value = false;
  }
});
</script>

<style scoped>
.panel {
  border-radius: 12px;
  border: 1px solid color-mix(in oklab, currentColor 12%, transparent);
  background: color-mix(in oklab, var(--color-bg, #fff) 100%, transparent);
  color: var(--popover-foreground, inherit);
  box-shadow:
    0 19px 30px rgba(0, 0, 0, 0.08),
    0 4px 12px rgb(0, 0, 0, 0.06);
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
  box-shadow:
    0 0 0 2px color-mix(in oklab, var(--ring, #3b82f6) 55%, transparent),
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
