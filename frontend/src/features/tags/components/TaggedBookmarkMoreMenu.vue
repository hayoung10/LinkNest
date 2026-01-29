<template>
  <div class="relatvie">
    <!-- 트리거 -->
    <button
      ref="triggerEl"
      type="button"
      class="inline-flex items-center justify-center size-8 rounded-md bg-white/85 backdrop-blur border border-white/60 shadow-sm hover:bg-white dark:bg-zinc-900/70 dark:border-zinc-700/60 dark:hover:bg-zinc-800 transition-colors"
      aria-haspopup="menu"
      :aria-expanded="open"
      @click.stop="toggle"
      title="메뉴"
    >
      ⋯
    </button>

    <!-- 메뉴 -->
    <teleport to="#modals" v-if="open">
      <div
        ref="panelEl"
        class="panel w-44"
        :style="panelStyle"
        role="menu"
        @click.stop
        @keydown.esc.prevent.stop="close"
      >
        <button class="menu-item" role="menuitem" @click="handleOpenWorkspace">
          Workspace에서 열기
        </button>

        <div class="divider" />

        <template v-if="mode === 'menu'">
          <button
            class="menu-item"
            role="menuitem"
            :disabled="isTagMutating"
            @click="handleDetach"
          >
            태그에서 제거
          </button>
          <button
            class="menu-item"
            role="menuitem"
            :disabled="isTagMutating || replaceCandidates.length === 0"
            @click="openReplace"
          >
            다른 태그로 교체
          </button>
        </template>

        <!-- 교체 모드 -->
        <template v-else>
          <div class="px-2 py-1.5 text-[12px] text-zinc-500 dark:text-zinc-400">
            교체할 태그 선택
          </div>

          <select
            v-model="targetTagId"
            class="w-full rounded-md px-2 py-2 text-sm bg-zinc-100 dark:bg-zinc-800 border border-zinc-300/70 dark:border-zinc-600/60 focus:outline-none focus:ring-2 focus:ring-violet-500/30"
          >
            <option value="" disabled>선택하세요</option>
            <option v-for="t in replaceCandidates" :key="t.id" :value="t.id">
              {{ t.name }} ({{ t.bookmarkCount }})
            </option>
          </select>

          <div class="mt-2 flex gap-2 px-1">
            <button
              type="button"
              class="menu-item flex-1 text-center"
              @click="cancelReplace"
              :disabled="isTagMutating"
            >
              취소
            </button>
            <button
              type="button"
              class="menu-item flex-1 text-center"
              @click="handleReplace"
              :disabled="isTagMutating || !canReplace"
            >
              적용
            </button>
          </div>
        </template>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeMount, onMounted, ref } from "vue";
import type { CSSProperties } from "vue";
import type { ID } from "@/types/common";
import { storeToRefs } from "pinia";
import { useTaggedBookmarksStore } from "@/stores/taggedBookmarks";
import { useTagsStore } from "@/stores/tags";
import { useToastStore } from "@/stores/toast";
import { getErrorMessage } from "@/utils/errorMessage";

const props = withDefaults(
  defineProps<{
    align?: "left" | "right";
    tagId: ID;
    bookmarkId: ID;
  }>(),
  { align: "right" },
);

const emit = defineEmits<{
  (e: "open-workspace"): void;
  (e: "tags-changed"): void;
}>();

const toast = useToastStore();
const taggedStore = useTaggedBookmarksStore();
const tagsStore = useTagsStore();

const { isMutating } = storeToRefs(taggedStore);
const { items: tagItems } = storeToRefs(tagsStore);

const isTagMutating = computed(
  () => !!(isMutating.value.detach || isMutating.value.replace),
);

// state
const mode = ref<"menu" | "replace">("menu");
const targetTagId = ref<ID | "">("");

const replaceCandidates = computed(() => {
  return tagItems.value.filter((t) => t.id !== props.tagId);
});

const canReplace = computed(() => {
  if (!targetTagId.value) return false;
  return String(targetTagId.value) !== String(props.tagId);
});

// position
const open = ref(false);
const triggerEl = ref<HTMLElement | null>(null);
const panelEl = ref<HTMLElement | null>(null);

const pos = ref({ top: 0, left: 0 });
const gap = 8;

const panelStyle = computed<CSSProperties>(() => ({
  position: "fixed",
  top: `${pos.value.top}px`,
  left: `${pos.value.left}px`,
  transform: "translate3d(0,0,0)",
}));

function updatePosition() {
  const trigger = triggerEl.value;
  const panel = panelEl.value;
  if (!trigger || !panel) return;

  const t = trigger.getBoundingClientRect();
  const pw = panel.offsetWidth || 176;
  const ph = panel.offsetHeight || 120;

  // 기본은 아래, 공간 없으면 위에 배치
  let top = t.bottom + gap;
  if (top + ph > window.innerHeight - 8) top = t.top - ph - gap;
  top = Math.max(8, Math.min(top, window.innerHeight - ph - 8));

  let left = props.align === "right" ? t.right - pw : t.left;
  left = Math.max(8, Math.min(left, window.innerWidth - pw - 8));

  pos.value = { top: Math.round(top), left: Math.round(left) };
}

function toggle() {
  open.value ? close() : openMenu();
}

function close() {
  if (!open.value) return;
  open.value = false;
  mode.value = "menu";
  targetTagId.value = "";
}

async function openMenu() {
  open.value = true;
  await nextTick();
  await new Promise(requestAnimationFrame);
  updatePosition();
}

function handleOpenWorkspace() {
  close();
  emit("open-workspace");
}

// actions
async function handleDetach() {
  try {
    await taggedStore.detachFromBookmarks([props.bookmarkId], {
      reload: false,
    });
    emit("tags-changed");
    close();
  } catch (e) {
    toast.error(getErrorMessage(e, "태그 제거에 실패했습니다."));
  }
}

function openReplace() {
  mode.value = "replace";
  targetTagId.value = "";
  nextTick(updatePosition);
}

function cancelReplace() {
  mode.value = "menu";
  targetTagId.value = "";
  nextTick(updatePosition);
}

async function handleReplace() {
  if (!canReplace.value) return;

  try {
    const toTagId = Number(targetTagId.value);
    await taggedStore.replaceOnBookmarks(toTagId, [props.bookmarkId], {
      reload: false,
    });
    emit("tags-changed");
    close();
  } catch (e) {
    toast.error(getErrorMessage(e, "태그 교체에 실패했습니다."));
  }
}

function onDocClick(e: MouseEvent) {
  if (!open.value) return;
  const t = e.target as Node | null;
  if (panelEl.value?.contains(t) || triggerEl.value?.contains(t)) return;
  close();
}

onMounted(() =>
  document.addEventListener("click", onDocClick, { capture: true }),
);

onBeforeMount(() =>
  document.removeEventListener("click", onDocClick, { capture: true } as any),
);
</script>

<style scoped>
.panel {
  z-index: 240;
  border-radius: 12px;
  border: 1px solid color-mix(in oklab, currentColor 12%, transparent);
  background: color-mix(in oklab, var(--color-bg, #fff) 100%, transparent);
  box-shadow:
    0 10px 30px rgba(0, 0, 0, 0.08),
    0 4px 12px rgba(0, 0, 0, 0.06);
  padding: 4px;
  font-size: 13px;
}

.menu-item {
  width: 100%;
  text-align: left;
  padding: 10px 10px;
  border-radius: 10px;
  transition: background 0.15s ease;
}

.menu-item:hover {
  background: var(--accent, color-mix(in oklab, currentColor 12%, transparent));
}

.menu-item:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.divider {
  height: 1px;
  margin: 6px 4px;
  background: color-mix(in oklab, currentColor 12%, transparent);
  border-radius: 1px;
}
</style>
