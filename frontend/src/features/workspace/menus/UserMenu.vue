<template>
  <div class="relative">
    <!-- 트리거 -->
    <button
      class="flex items-center gap-3 w-full px-2 py-2 rounded-md hover:bg-accent transition-colors"
      @click.stop="open = !open"
      aria-haspopup="menu"
      :aria-expanded="open"
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
        <span class="text-xs text-muted-foreground truncat w-full">{{
          user.email
        }}</span>
      </span>

      <!-- 화살표 아이콘 -->
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
    <teleport to="body">
      <div
        v-if="open"
        class="absolute bottom-full mb-2 right-0 w-56 rounded-md border bg-popover text-popover-foreground shadow-lg z-50 overflow-hidden"
        role="menu"
        @click.stop
      >
        <!-- 유저 정보 -->
        <div class="px-3 py-2">
          <p class="text-sm font-medium">{{ user.name }}</p>
          <p class="text-xs text-muted-foreground truncate">{{ user.email }}</p>
        </div>
        <div class="h-px bg-border" />

        <!-- 마이페이지 -->
        <button
          class="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-accent"
          role="menuitem"
          @click="open = false"
        >
          <svg
            class="size-4"
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
        <button
          class="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-accent"
          role="menuitem"
          @click="open = false"
        >
          <svg
            class="size-4"
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

        <div class="h-px bg-border" />

        <!-- 로그아웃 -->
        <button
          class="w-full flex items-center gap-2 px-3 py-2 text-sm text-destructive hover:bg-accent/70"
          role="menuitem"
          @click="open = false"
        >
          <svg
            class="size-4"
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
import { computed, onBeforeUnmount, onMounted, ref } from "vue";

const user = { name: "홍길동", email: "hong@example.com", profileImageUrl: "" }; // 임시 유저
const initials = computed(() => user.name.charAt(0));
const open = ref(false);

function onDocClick() {
  open.value = false;
}
onMounted(() => document.addEventListener("click", onDocClick));
onBeforeUnmount(() => document.removeEventListener("click", onDocClick));
</script>
