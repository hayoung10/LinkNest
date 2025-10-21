<template>
  <div class="relative">
    <!-- 트리거 -->
    <button
      ref="triggerEl"
      class="flex items-center gap-3 w-full px-2 py-2 rounded-md hover:bg-accent transition-colors"
      :aria-expanded="open"
      aria-haspopup="menu"
      aria-controls="user-menu-panel"
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
      <svg
        class="size-4 text-muted-foreground"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
      >
        <path d="M6 14l6-6 6 6" />
      </svg>
    </button>

    <!-- 드롭다운 메뉴 -->
    <Teleport to="body" v-if="open">
      <div
        id="user-menu-panel"
        class="panel w-56"
        :style="panelStyle"
        role="menu"
        @click.stop
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
        <button class="menu-item" role="menuitem" @click="handleProfile">
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
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import type { CSSProperties } from "vue";

const user = { name: "홍길동", email: "hong@example.com", profileImageUrl: "" }; // 임시 유저
const initials = computed(() => user.name.charAt(0));

const open = ref(false);
const triggerEl = ref<HTMLElement | null>(null);

const pos = ref({ top: 0, left: 0 });
const panelWidth = 224;
const gap = 8;

const panelStyle = computed<CSSProperties>(() => ({
  position: "fixed",
  top: `${pos.value.top}px`,
  left: `${pos.value.left}px`,
  right: "auto",
  bottom: "auto",
  width: `${panelWidth}px`,
  transform: "translate3d(0,0,0)",
  fontSize: "14px",
  lineHeight: "20px",
}));

let ignoreNextDocClick = false;

async function toggleMenu() {
  open.value = !open.value;
  if (open.value) {
    // 열리는 순간 문서클릭 무시
    ignoreNextDocClick = true;
    await nextTick();

    // 위치 계산은 1 프레임 뒤에 (DOM 실제 생성 이후)
    requestAnimationFrame(() => {
      updatePosition();

      // 다음 프레임에 문서클릭 허용
      requestAnimationFrame(() => {
        ignoreNextDocClick = false;
      });
    });
  }
}

function updatePosition() {
  const t = triggerEl.value?.getBoundingClientRect();
  const panel = document.getElementById("user-menu-panel");
  const ph = panel ? panel.offsetHeight : 0;
  if (!t) return;

  // 기본은 위, 공간 없으면 아래에 배치
  let top = t.top - ph - gap;
  if (top < 8) top = t.bottom + gap;
  top = Math.max(8, Math.min(top, window.innerHeight - ph - 8));

  let left = t.right - panelWidth; // 우측 정렬
  left = Math.max(8, Math.min(left, window.innerWidth - panelWidth - 8));

  pos.value = { top: Math.round(top), left: Math.round(left) };
}

// 바깥 클릭 닫기
function onDocClick(e: MouseEvent) {
  if (!open.value) return;
  const t = e.target as Node | null;
  const panel = document.getElementById("user-menu-panel");
  if (panel?.contains(t as Node)) return;
  if (triggerEl.value?.contains(t as Node)) return;
  open.value = false;
}
onMounted(() =>
  document.addEventListener("click", onDocClick, { capture: true })
);
onBeforeUnmount(() =>
  document.removeEventListener("click", onDocClick, { capture: true })
);

// 액션 핸들러
function handleProfile() {
  open.value = false;
}
function handleSettings() {
  open.value = false;
}
function handleLogout() {
  open.value = false;
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
