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

        <button class="menu-item" role="menuitem" disabled title="TODO">
          태그에서 제거
        </button>
        <button class="menu-item" role="menuitem" disabled title="TODO">
          다른 태그로 교체
        </button>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeMount, onMounted, ref } from "vue";
import type { CSSProperties } from "vue";

const props = withDefaults(
  defineProps<{
    align?: "left" | "right";
  }>(),
  { align: "right" },
);

const emit = defineEmits<{ (e: "open-workspace"): void }>();

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

// 바깥 클릭 닫기
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
