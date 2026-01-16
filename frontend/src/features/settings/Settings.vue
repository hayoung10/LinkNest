<template>
  <div class="h-full w-full px-6 py-6 flex flex-col">
    <!-- 헤더 -->
    <header class="flex items-start gap-4 mb-6">
      <!-- 좌: 패널 닫기 -->
      <button
        type="button"
        class="p-2 rounded-md transition-colors duration-150 text-muted-foreground hover:bg-zinc-200/70 dark:hover:bg-zinc-700/60 hover:text-foreground"
        aria-label="닫기"
        @click="$emit('close')"
      >
        <CloseIcon class="size-5" />
      </button>

      <!-- 우: 제목 + 설명 -->
      <div class="flex-1">
        <h1 class="text-[17px] font-semibold">설정</h1>
        <p class="mt-1 text-sm text-neutral-500">
          계정·보안·환경 설정을 한 곳에서 관리하세요.
        </p>
      </div>
    </header>

    <!-- 탭 버튼 영역 -->
    <div
      class="w-full rounded-full bg-zinc-100 border-2 border-zinc-100 px-4 py-2 mb-3"
    >
      <!-- 탭 바 -->
      <nav class="flex items-center gap-3">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          type="button"
          class="px-4 py-2 rounded-full text-sm font-medium transition-all"
          :class="
            activeId === tab.id
              ? 'bg-gradient-to-br from-blue-500 to-indigo-600 text-white shadow'
              : 'text-muted-foreground hover:bg-background hover:text-foreground'
          "
          @click="activeId = tab.id"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- 탭 본문 -->
    <div class="flex-1 overflow-y-auto">
      <!-- 계정 탭 -->
      <ProfileSection v-if="activeId === 'profile'" />

      <!-- 보안 탭 -->
      <SecuritySection v-else-if="activeId === 'security'" />

      <!-- 환경 설정 탭-->
      <PreferencesSection v-else />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import ProfileSection from "./components/ProfileSection.vue";
import SecuritySection from "./components/SecuritySection.vue";
import PreferencesSection from "./components/PreferencesSection.vue";
import CloseIcon from "@/components/icons/CloseIcon.vue";

type TabId = "profile" | "security" | "preferences";

const emit = defineEmits<{ (e: "close"): void }>();

const tabs: { id: TabId; label: string }[] = [
  { id: "profile", label: "계정" },
  { id: "security", label: "보안" },
  { id: "preferences", label: "환경 설정" },
];

const activeId = ref<TabId>("profile");
</script>
