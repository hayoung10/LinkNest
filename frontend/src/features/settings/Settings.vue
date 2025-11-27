<template>
  <section class="h-full flex flex-col bg-card text-card-foreground">
    <!-- 헤더 -->
    <header class="sticky top-0 z-10 bg-card border-b border-border">
      <div class="flex items-center justify-between px-4 py-3">
        <div class="min-w-0">
          <h1 class="text-[17px] font-semibold truncate">설정</h1>
          <p class="mt-0.5 text-xs text-muted-foreground">
            계정·보안·환경 설정을 한 곳에서 관리하세요.
          </p>
        </div>

        <!-- 우측 상단: 닫기 -->
        <button
          type="button"
          class="ml-l p-2 rounded-md hover:bg-accent"
          aria-label="설정 닫기"
          @click="$emit('close')"
        >
          ✕
        </button>
      </div>

      <!-- 탭 -->
      <div class="px-4 border-t border-border/80 bg-card/90 backdrop-blur">
        <nav class="flex items-center gap-2 text-sm h-10" aria-label="설정 탭">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            type="button"
            class="px-3 py-1.5 rounded-full transition-colors whitespace-nowrap"
            :class="
              activeId === tab.id
                ? 'bg-neutral-900 text-white dark:bg-neutral-100 dark:text-neutral-900'
                : 'text-muted-foreground hover:bg-accent'
            "
            @click="activeId = tab.id"
          >
            {{ tab.label }}
          </button>
        </nav>
      </div>
    </header>

    <!-- 본문 -->
    <div class="flex-1 min-h-0 overflow-y-auto px-5 py-4 space-y-6">
      <ProfileSection v-if="activeId === 'profile'" />
      <SecuritySection v-else-if="activeId === 'security'" />
      <PreferencesSection v-else />
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from "vue";
import ProfileSection from "./components/ProfileSection.vue";
import SecuritySection from "./components/SecuritySection.vue";
import PreferencesSection from "./components/PreferencesSection.vue";

type TabId = "profile" | "security" | "preferences";

const emit = defineEmits<{ (e: "close"): void }>();

const tabs: { id: TabId; label: string }[] = [
  { id: "profile", label: "계정" },
  { id: "security", label: "보안" },
  { id: "preferences", label: "환경 설정" },
];

const activeId = ref<TabId>("profile");
</script>
