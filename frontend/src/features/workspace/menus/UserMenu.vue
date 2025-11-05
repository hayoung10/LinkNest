<template>
  <div class="relative">
    <!-- 트리거 -->
    <button
      ref="triggerEl"
      class="flex items-center gap-3 w-full px-2 py-2 rounded-md hover:bg-accent transition-colors"
      :aria-expanded="menuOpen"
      aria-haspopup="menu"
      :aria-controls="panelId"
      @click.stop="toggleMenu"
    >
      <!-- 아바타 -->
      <div
        class="size-8 shrink-0 rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 text-white grid place-items-center overflow-hidden"
      >
        <img
          v-if="user.profileImageUrl"
          :src="user.profileImageUrl"
          :alt="user.name"
          class="w-full h-full object-cover"
        />
        <span v-else class="text-sm font-medium">{{ initials }}</span>
      </div>

      <!-- 이름 + 이메일 -->
      <span class="flex flex-col items-start min-w-0 flex-1">
        <span class="text-sm font-medium truncate w-full">{{ user.name }}</span>
        <span class="text-xs text-muted-foreground truncate w-full">{{
          user.email
        }}</span>
      </span>

      <!-- 화살표 아이콘(^) -->
      <ChevronIcon
        :size="16"
        direction="up"
        class="text-muted-foreground"
        aria-hidden="true"
      />
    </button>

    <!-- 드롭다운 메뉴 -->
    <teleport to="#modals" v-if="menuOpen">
      <div
        :id="panelId"
        class="panel w-56"
        :style="panelStyle"
        role="menu"
        @click.stop
        @keydown.esc.prevent.stop="closeMenu"
      >
        <!-- 유저 정보 -->
        <div class="px-4 pt-4 pb-3">
          <p class="text-[15px] font-semibold leading-[22px]">
            {{ user.name }}
          </p>
          <p class="mt-1 text-xs text-muted-foreground leading-5 truncate">
            {{ user.email }}
          </p>
        </div>

        <div class="divider" />

        <!-- 마이페이지 -->
        <button
          ref="firstItemRef"
          class="menu-item"
          role="menuitem"
          @click="handleProfile"
        >
          <svg
            class="menu-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          마이페이지
        </button>

        <!-- 계정 설정 -->
        <button class="menu-item" role="menuitem" @click="handleSettings">
          <svg
            class="menu-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <circle cx="12" cy="12" r="3" />
            <path
              d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33h0A1.65 1.65 0 0 0 9 3.09V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51h0a1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82v0A1.65 1.65 0 0 0 20.91 11H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1Z"
            />
          </svg>
          계정 설정
        </button>

        <div class="divider" />

        <!-- 로그아웃 -->
        <button
          class="menu-item menu-destructive"
          role="menuitem"
          @click="handleLogout"
        >
          <svg
            class="menu-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <path d="M16 17l5-5-5-5" />
            <path d="M21 12H9" />
          </svg>
          로그아웃
        </button>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import ChevronIcon from "@/components/icons/ChevronIcon.vue";
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import type { CSSProperties } from "vue";

const user = { name: "홍길동", email: "hong@example.com", profileImageUrl: "" }; // 임시 유저
const initials = computed(() => user.name.charAt(0));

const menuOpen = ref(false);
const triggerEl = ref<HTMLElement | null>(null);
const firstItemRef = ref<HTMLElement | null>(null);

const ignoreNextDocClick = ref(false);
const panelId = "user-menu-panel";

const pos = ref({ top: 0, left: 0 });
const panelWidth = 224;
const gap = 8;

const panelStyle = computed<CSSProperties>(() => ({
  position: "fixed",
  top: `${pos.value.top}px`,
  left: `${pos.value.left}px`,
  width: `${panelWidth}px`,
  transform: "translate3d(0,0,0)",
}));

function updatePosition() {
  const trigger = triggerEl.value;
  const panel = document.getElementById(panelId);
  if (!trigger || !panel) return;

  const t = trigger.getBoundingClientRect();
  const ph = panel?.offsetHeight ?? 0;

  // 기본은 위, 공간 없으면 아래에 배치
  let top = t.top - ph - gap;
  if (top < 8) top = t.bottom + gap;
  top = Math.max(8, Math.min(top, window.innerHeight - ph - 8));

  let left = t.right - panelWidth; // 우측 정렬
  left = Math.max(8, Math.min(left, window.innerWidth - panelWidth - 8));

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

onMounted(() =>
  document.addEventListener("click", onDocClick, { capture: true })
);
onBeforeUnmount(() =>
  document.removeEventListener("click", onDocClick, { capture: true })
);

// 액션 핸들러
function handleProfile() {
  closeMenu();
}
function handleSettings() {
  closeMenu();
}
function handleLogout() {
  closeMenu();
}
</script>

<style scoped>
/* 공통 패널 */
.panel {
  position: fixed;
  z-index: 220;
  border-radius: 12px;
  border: 1px solid color-mix(in oklab, currentColor 12%, transparent);
  background: color-mix(in oklab, var(--color-bg, #fff) 100%, transparent);
  color: var(--popover-foreground, inherit);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08), 0 4px 12px rgba(0, 0, 0, 0.06);
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
  background: var(--accent, color-mix(in oklab, currentColor 12%, transparent));
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
