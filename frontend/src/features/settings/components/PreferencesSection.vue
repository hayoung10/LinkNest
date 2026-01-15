<template>
  <section class="space-y-6">
    <!-- 카드 1: 보기 설정 -->
    <div
      class="rounded-2xl border border-zinc-200 bg-white px-6 py-5 space-y-6"
    >
      <header class="space-y-1">
        <h2 class="text-lg font-semibold text-zinc-900">보기 설정</h2>
        <p class="text-sm text-zinc-500">
          북마크가 표시되는 기본 방식을 설정합니다.
        </p>
      </header>

      <!-- 기본 북마크 정렬 -->
      <div class="space-y-2">
        <p class="text-sm font-medium text-zinc-900">기본 북마크 정렬</p>
        <p class="text-xs text-zinc-500">
          북마크 목록을 어떤 순서로 정렬할지 선택하세요.
        </p>

        <div class="mt-2 grid gap-2 sm:grid-cols-3">
          <button
            v-for="option in sortOptions"
            :key="option.value"
            type="button"
            :disabled="!loaded || isSaving"
            class="flex items-center justify-center rounded-xl border px-3 py-2 text-sm transition"
            :class="
              option.value === defaultBookmarkSort
                ? 'border-zinc-900 bg-zinc-900 text-white'
                : 'border-zinc-200 bg-white text-zinc-700 hover:bg-zinc-50'
            "
            @click="changeDefaultSort(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <hr class="border-zinc-200" />

      <!-- 기본 레이아웃 -->
      <div class="space-y-2">
        <p class="text-sm font-medium text-zinc-900">기본 레이아웃</p>
        <p class="text-xs text-zinc-500">
          북마크 목록을 기본으로 어떻게 표시할지 선택하세요.
        </p>

        <div class="mt-2 grid gap-2 sm:grid-cols-2">
          <button
            v-for="layout in layoutOptions"
            :key="layout.value"
            type="button"
            :disabled="!loaded || isSaving"
            class="flex items-center justify-center gap-2 rounded-xl border px-3 py-2 text-sm transition"
            :class="
              layout.value === defaultLayout
                ? 'border-zinc-900 bg-zinc-900 text-white'
                : 'border-zinc-200 bg-white text-zinc-700 hover:bg-zinc-50'
            "
            @click="changeDefaultLayout(layout.value)"
          >
            <span>{{ layout.label }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 카드 2: 동작 설정 -->
    <div class="rounded-2xl border border-zinc-200 bg-white p-6 space-y-4">
      <header class="space-y-1">
        <h3 class="text-base font-semibold text-zinc-900">동작 설정</h3>
        <p class="text-sm text-zinc-500">
          링크를 열 때의 기본 동작을 설정합니다.
        </p>
      </header>

      <!-- 북마크 새 탭에서 열기 -->
      <label class="flex items-start justify-between gap-3">
        <div>
          <p class="text-sm font-medium text-zinc-900">
            북마크를 새 탭에서 열기
          </p>
          <p class="text-xs text-zinc-500">
            선택하면 북마크를 클릭할 때마다 새 탭에서 링크가 열립니다.
          </p>
        </div>

        <button
          type="button"
          class="relative inline-flex h-6 w-11 items-center rounded-full transition"
          :class="openInNewTab ? 'bg-zinc-900' : 'bg-zinc-300'"
          @click="updateOpenInNewTab"
          :disabled="!loaded || isSaving"
        >
          <span
            class="inline-block h-4 w-4 transform rounded-full bg-white transition"
            :class="openInNewTab ? 'translate-x-5' : 'translate-x-1'"
          />
        </button>
      </label>
    </div>
  </section>
</template>

<script setup lang="ts">
import { usePreferencesStore } from "@/stores/preferences";
import { useToastStore } from "@/stores/toast";
import type { BookmarkSortOption, LayoutOption } from "@/types/common";
import { storeToRefs } from "pinia";
import { ref } from "vue";

// 기본 북마크 정렬 옵션
const sortOptions: { value: BookmarkSortOption; label: string }[] = [
  { value: "NEWEST", label: "최신순" },
  { value: "OLDEST", label: "오래된순" },
  { value: "TITLE", label: "제목순" },
];

// 기본 레이아웃 옵션
const layoutOptions: { value: LayoutOption; label: string }[] = [
  { value: "CARD", label: "카드형" },
  { value: "LIST", label: "리스트형" },
];

const toast = useToastStore();
const preferences = usePreferencesStore();

const { defaultBookmarkSort, defaultLayout, openInNewTab, loaded } =
  storeToRefs(preferences);

// 로딩/저장 상태
const isSaving = ref(false);

// 이벤트 핸들러
const changeDefaultSort = async (value: BookmarkSortOption) => {
  if (!loaded.value || isSaving.value) return;
  if (defaultBookmarkSort.value === value) return;

  const prev = defaultBookmarkSort.value;
  isSaving.value = true;

  try {
    await preferences.update({ defaultBookmarkSort: value });
    defaultBookmarkSort.value = value;
  } catch (e) {
    console.error("기본 정렬 업데이트 실패:", e);
    toast.error("기본 북마크 정렬 설정에 실패했습니다.");
    defaultBookmarkSort.value = prev;
  } finally {
    isSaving.value = false;
  }
};

const changeDefaultLayout = async (value: LayoutOption) => {
  if (!loaded.value || isSaving.value) return;
  if (defaultLayout.value === value) return;

  const prev = defaultLayout.value;
  isSaving.value = true;

  try {
    await preferences.update({ defaultLayout: value });
    defaultLayout.value = value;
  } catch (e) {
    console.error("기본 레이아웃 업데이트 실패:", e);
    toast.error("기본 레이아웃 설정에 실패했습니다.");
    defaultLayout.value = prev;
  } finally {
    isSaving.value = false;
  }
};

const updateOpenInNewTab = async () => {
  if (!loaded.value || isSaving.value) return;
  const prev = openInNewTab.value;
  const next = !openInNewTab.value;

  openInNewTab.value = next;

  isSaving.value = true;
  try {
    await preferences.update({ openInNewTab: next });
  } catch (e) {
    console.error("새 탭 열기 설정 업데이트 실패:", e);
    toast.error("새 탭 열기 설정에 실패했습니다.");
    openInNewTab.value = prev;
  } finally {
    isSaving.value = false;
  }
};
</script>
