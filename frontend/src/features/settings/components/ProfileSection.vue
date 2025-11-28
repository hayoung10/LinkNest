<template>
  <section class="space-y-6">
    <!-- 카드 1: 프로필/이름/이메일 -->
    <div
      class="rounded-2xl border border-zinc-200 bg-white px-6 py-5 space-y-6"
    >
      <!-- 제목 -->
      <header class="space-y-1">
        <h2 class="text-lg font-semibold text-zinc-900">프로필</h2>
        <p class="text-sm text-zinc-500">프로필 사진과 이름을 관리합니다</p>
      </header>

      <!-- 아바타 + 버튼 -->
      <div class="flex items-center gap-6">
        <!-- 아바타 -->
        <div
          class="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 text-3xl font-semibold text-white"
        >
          <span>{{ avatar }}</span>
        </div>

        <!-- 업로드 버튼 -->
        <div class="space-y-2">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl border border-zinc-200 bg-white px-4 py-2 text-sm font-medium text-zinc-800 shadow-sm hover:bg-zinc-50"
          >
            <!-- 업로드 아이콘 -->
            <span class="flex h-5 w-5 items-center justify-center">
              <svg class="h-5 w-5" viewBox="0 0 16 16" aria-hidden="true">
                <path
                  d="M8 3.333 4.667 6.667h2V10h2.666V6.667h2L8 3.333Z"
                  fill="currentColor"
                />
                <path
                  d="M3.333 11.333h9.334v1.334H3.333z"
                  fill="currentColor"
                />
              </svg>
            </span>
            <span>사진 변경</span>
          </button>
          <p class="text-xs text-zinc-400">JPG, PNG 또는 GIF (최대 5MB)</p>
        </div>
      </div>

      <hr class="border-zinc-200" />

      <!-- 이름 / 이메일 폼 -->
      <form class="space-y-4" @submit.prevent="onSubmit">
        <!-- 이름 -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-zinc-800">이름</label>
          <div class="flex gap-3">
            <input
              v-model="editableName"
              type="text"
              class="flex-1 rounded-xl border border-transparent bg-zinc-50 px-4 py-3 text-sm text-zinc-900 outlnie-none ring-1 ring-zinc-200 focus:border-indigo-500 focus:ring-indigo-500"
            />
            <button
              type="submit"
              class="inline-flex items-center justify-center rounded-xl bg-zinc-950 px-5 text-sm font-smibold text-white hover:bg-zinc-900"
            >
              저장
            </button>
          </div>
        </div>

        <!-- 이메일 -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-zinc-800">이메일</label>
          <div class="relative">
            <input
              :value="email"
              type="email"
              disabled
              class="w-full cursor-not-allowed rounded-xl border border-transparent bg-zinc-50 px-4 py-3 text-sm text-zinc-400 outline-none ring-1 ring-zinc-200"
            />
            <!-- 이메일 아이콘 -->
            <div
              class="pointer-events-none absolute inset-y-0 right-4 flex items-center text-zinc-400"
            >
              <svg class="h-7 w-7" viewBox="0 0 20 20" aria-hidden="true">
                <path
                  d="M3.75 4.5h12.5A1.75 1.75 0 0 1 18 6.25v7.5A1.75 1.75 0 0 1 16.25 15.5H3.75A1.75 1.75 0 0 1 2 13.75v-7.5A1.75 1.75 0 0 1 3.75 4.5Zm0 1.5 6.25 3.75L16.25 6h-12.5Zm0 1.31v6.44h12.5V7.31l-6.06 3.64a.75.75 0 0 1-.78 0L3.75 7.31Z"
                  fill="currentColor"
                />
              </svg>
            </div>
          </div>
          <p class="text-xs text-zinc-400">
            이메일은 Google 계정과 연동되어 변경할 수 없습니다
          </p>
        </div>
      </form>
    </div>

    <!-- 카드 2: 연동된 계정 -->
    <section
      class="rounded-2xl border border-zinc-200 bg-white px-6 py-5 shadow-sm"
    >
      <div class="space-y-1">
        <h3 class="text-base font-semibold text-zinc-900">연동된 계정</h3>
        <p class="text-sm text-zinc-500">
          소셜 로그인 계정 연동 상태를 확인합니다.
        </p>
      </div>

      <div
        class="mt-4 flex items-center justify-between gap-4 rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3 sm:px-5 sm:py-4"
      >
        <div class="flex items-center gap-3">
          <div
            class="flex h-11 w-11 items-center justify-center rounded-full bg-indigo-100 text-indigo-600"
          >
            <!-- 이메일 아이콘 -->
            <svg class="h-7 w-7" viewBox="0 0 20 20" aria-hidden="true">
              <path
                d="M3.75 4.5h12.5A1.75 1.75 0 0 1 18 6.25v7.5A1.75 1.75 0 0 1 16.25 15.5H3.75A1.75 1.75 0 0 1 2 13.75v-7.5A1.75 1.75 0 0 1 3.75 4.5Zm0 1.5 6.25 3.75L16.25 6h-12.5Zm0 1.31v6.44h12.5V7.31l-6.06 3.64a.75.75 0 0 1-.78 0L3.75 7.31Z"
                fill="currentColor"
              />
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-zinc-900">Google</p>
            <p class="text-xs text-zinc-500">{{ email }}</p>
          </div>
        </div>

        <span
          class="inline-flex items-center rounded-full bg-emerald-100 px-4 py-2 text-xs font-medium text-emerald-700"
          >연동됨</span
        >
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";

const user = { name: "홍길동", email: "hong@example.com", profileImageUrl: "" }; // 임시유저

const editableName = ref(user.name);

const avatar = computed(() => {
  const n = editableName.value?.trim();
  return n ? n[0] : "홍";
});

const email = computed(() => user.email);

const onSubmit = () => {
  user.name = editableName.value.trim();
  // TODO: API 연동
  console.log("이름 변경 버튼:", user.name);
};
</script>
